package id.belajarsd.app;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** Runs real banks from every chapter through session selection. */
class SessionRepeatTest {
  private static final Set<String> MATH = Set.of("Matematika", "Hitung Cepat", "Mathematics in English");

  private static List<Question> bank(CurriculumCatalog.Seed seed) {
    String grade = "Kelas " + seed.grade();
    Lesson lesson = new Lesson(new Subject(seed.subject(), "", "", "", 1), seed.title(), grade, "", 10);
    List<Question> rows = new ArrayList<>();
    if (MATH.contains(seed.subject())) {
      for (var item : MathQuestionBank.forChapter(seed.subject(), grade, seed.title()))
        rows.add(new Question(lesson, item.prompt(), String.join("|||", item.options()), 0, item.explanation(), item.essay(), item.level(), item.longMethod(), item.quickMethod(), item.form()));
    } else {
      for (var item : CurriculumQuestionBank.forLesson(lesson))
        rows.add(new Question(lesson, item.prompt(), String.join("|||", item.options()), 0, item.explanation()));
    }
    long id = 1;
    for (Question row : rows) ReflectionTestUtils.setField(row, "id", id++);
    return rows;
  }


  @Test
  void noSessionInAnyChapterHoldsTheSameQuestionTwice() {
    int sessions = 0;
    for (CurriculumCatalog.Seed seed : CurriculumCatalog.seeds()) {
      List<Question> bank = bank(seed);
      Map<Long, Instant> lastSeen = new HashMap<>();
      Instant clock = Instant.parse("2026-01-01T00:00:00Z");
      for (int round = 0; round < 12; round++) {
        for (int size : List.of(10, 5)) {
          List<Question> session = QuestionSelector.select(bank, size, new Random(round), lastSeen);
          String where = seed.subject() + " " + seed.grade() + " " + seed.title() + " sesi " + round;
          assertEquals(session.size(), session.stream().map(QuestionSelector::sameQuestionKey).distinct().count(), where);
          assertFalse(session.isEmpty(), where);
          sessions++;
          clock = clock.plusSeconds(60);
          for (Question question : session) lastSeen.put(question.getId(), clock);
        }
      }
    }
    assertTrue(sessions > 7000, "Sesi yang diperiksa: " + sessions);
  }

  @Test
  void mathTestsStayFullAndNeverReuseTheSameNumbers() {
    for (CurriculumCatalog.Seed seed : CurriculumCatalog.seeds()) {
      if (!MATH.contains(seed.subject())) continue;
      List<Question> bank = bank(seed);
      Map<Long, Instant> lastSeen = new HashMap<>();
      Instant clock = Instant.parse("2026-01-01T00:00:00Z");
      for (int round = 0; round < 12; round++) {
        List<Question> test = QuestionSelector.select(bank, 10, new Random(round), lastSeen);
        String where = seed.subject() + " " + seed.grade() + " " + seed.title() + " sesi " + round;
        assertEquals(15, test.size(), where);
        assertEquals(5, test.stream().filter(Question::isEssay).count(), where);
        assertEquals(15, test.stream().map(SessionRepeatTest::numbers).distinct().count(), where + " " + test.stream().map(Question::getPrompt).toList());
        clock = clock.plusSeconds(60);
        for (Question question : test) lastSeen.put(question.getId(), clock);
      }
    }
  }

  /** The sorted numbers in a prompt, or the prompt itself when it has none. */
  private static String numbers(Question question) {
    List<String> found = java.util.regex.Pattern.compile("\\d+(?:[.,]\\d+)*").matcher(question.getPrompt()).results().map(java.util.regex.MatchResult::group).sorted().toList();
    return found.isEmpty() ? question.getPrompt() : String.join(" ", found);
  }

  @Test
  void aFactWaitsItsTurnEvenWhenReworded() {
    Lesson lesson = new Lesson(new Subject("IPAS", "", "", "", 1), "Bab 1 — Contoh", "Kelas 3", "", 10);
    List<Question> bank = new ArrayList<>();
    long id = 1;
    for (int wording = 0; wording < 5; wording++) for (int fact = 0; fact < 20; fact++) {
      Question question = new Question(lesson, "Kalimat " + wording + " fakta " + fact, "a|||b", 0, "Fakta " + fact);
      ReflectionTestUtils.setField(question, "id", id++);
      bank.add(question);
    }
    Map<Long, Instant> lastSeen = new HashMap<>();
    List<Question> first = QuestionSelector.select(bank, 10, new Random(1), lastSeen);
    first.forEach(q -> lastSeen.put(q.getId(), Instant.parse("2026-01-01T00:00:00Z")));
    List<Question> second = QuestionSelector.select(bank, 10, new Random(2), lastSeen);
    Set<String> firstFacts = first.stream().map(Question::getExplanation).collect(Collectors.toSet());
    assertEquals(10, firstFacts.size());
    assertTrue(new HashSet<>(second.stream().map(Question::getExplanation).toList()).stream().noneMatch(firstFacts::contains));
  }

  @Test
  void everyChapterHasAFullSessionOfItsOwnQuestions() {
    for (CurriculumCatalog.Seed seed : CurriculumCatalog.seeds()) {
      if (MATH.contains(seed.subject())) continue;
      List<Question> bank = bank(seed);
      String where = seed.subject() + " " + seed.grade() + " " + seed.title();
      if (!(seed.subject().equals("IPAS") && seed.grade() <= 2))
        assertFalse(FactBank.forChapter(seed.subject(), seed.grade(), seed.title()).isEmpty(), where + " belum punya soal yang ditulis khusus untuk babnya");
      Set<String> keys = bank.stream().map(QuestionSelector::sameQuestionKey).collect(Collectors.toSet());
      assertTrue(keys.size() >= 10, where + " hanya punya " + keys.size() + " soal berbeda");
      assertEquals(bank.size(), keys.size(), where + " menulis soal yang sama lebih dari sekali");
      for (Question question : bank) {
        String[] options = question.getOptions();
        assertEquals(4, java.util.Arrays.stream(options).distinct().count(), where + ": " + question.getPrompt());
      }
    }
  }

  /** Otherwise a student practising two chapters of one subject on the same day would meet the same question. */
  @Test
  void chaptersOfOneGradeNeverShareAQuestion() {
    Map<String, Map<String, String>> owners = new HashMap<>();
    for (CurriculumCatalog.Seed seed : CurriculumCatalog.seeds()) {
      if (MATH.contains(seed.subject())) continue;
      Map<String, String> owner = owners.computeIfAbsent(seed.subject() + " kelas " + seed.grade(), key -> new HashMap<>());
      for (Question question : bank(seed)) {
        String previous = owner.putIfAbsent(QuestionSelector.sameQuestionKey(question), seed.title());
        assertTrue(previous == null || previous.equals(seed.title()),
            seed.subject() + " kelas " + seed.grade() + ": " + seed.title() + " memakai soal dari " + previous + ": " + question.getPrompt());
      }
    }
  }

  /** A student repeats sessions in every chapter of a subject all day long: nothing ever comes back that day. */
  @Test
  void aWholeDayOfSessionsNeverRepeatsAQuestion() {
    Map<String, List<CurriculumCatalog.Seed>> bySubjectGrade = new java.util.LinkedHashMap<>();
    for (CurriculumCatalog.Seed seed : CurriculumCatalog.seeds()) bySubjectGrade.computeIfAbsent(seed.subject() + " kelas " + seed.grade(), key -> new ArrayList<>()).add(seed);
    long id = 1;
    for (var entry : bySubjectGrade.entrySet()) {
      Set<String> shownToday = new HashSet<>();
      List<List<Question>> banks = new ArrayList<>();
      for (CurriculumCatalog.Seed seed : entry.getValue()) {
        List<Question> bank = bank(seed);
        for (Question question : bank) ReflectionTestUtils.setField(question, "id", id++);
        banks.add(bank);
      }
      for (int round = 0; round < 40; round++) {
        boolean anything = false;
        for (List<Question> bank : banks) {
          List<Question> session = QuestionSelector.select(bank, round % 2 == 0 ? 10 : 5, new Random(round), Map.of(), shownToday);
          for (Question question : session) {
            assertTrue(shownToday.add(QuestionSelector.sameQuestionKey(question)), entry.getKey() + " mengulang soal pada hari yang sama: " + question.getPrompt());
            anything = true;
          }
        }
        if (!anything) break;
      }
      for (List<Question> bank : banks)
        assertTrue(QuestionSelector.select(bank, 10, new Random(99), Map.of(), shownToday).isEmpty(), entry.getKey() + ": soal hari ini harus sudah habis");
    }
  }

  @Test
  void theNextDayStartsWithQuestionsNotSeenBefore() {
    Lesson lesson = new Lesson(new Subject("IPAS", "", "", "", 1), "Bab 1 — Contoh", "Kelas 3", "", 10);
    List<Question> bank = new ArrayList<>();
    for (int fact = 0; fact < 25; fact++) {
      Question question = new Question(lesson, "Soal " + fact, "a" + fact + "|||b|||c|||d", 0, "Fakta " + fact);
      ReflectionTestUtils.setField(question, "id", (long) fact + 1);
      bank.add(question);
    }
    Map<Long, Instant> lastSeen = new HashMap<>();
    QuestionSelector.select(bank, 10, new Random(1), lastSeen).forEach(q -> lastSeen.put(q.getId(), Instant.parse("2026-01-01T08:00:00Z")));
    List<Question> nextDay = QuestionSelector.select(bank, 10, new Random(2), lastSeen, Set.of());
    assertTrue(nextDay.stream().noneMatch(q -> lastSeen.containsKey(q.getId())), "Hari berikutnya mendahulukan soal yang belum pernah muncul");
  }
}
