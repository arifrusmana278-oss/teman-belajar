package id.belajarsd.app;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Chooses the questions for one practice session. */
final class QuestionSelector {
  static final int FULL_TEST_CHOICES = 10;
  private static final int FULL_TEST_ESSAYS = 5;
  private static final Pattern DIGITS = Pattern.compile("\\d[\\d.,]*\\d|\\d");

  private QuestionSelector() {}

  /**
   * A full test is 10 choice questions from easier to harder (3/4/3) followed by 5 essays. A shorter practice of
   * {@code size} questions keeps that shape with one essay as its last hard question. Banks without levels or essays
   * are a plain draw of {@code size}. Questions never seen come first, then the ones seen longest ago, and each
   * level spreads across different question forms. No session holds the same question twice, so a bank with fewer
   * distinct questions than {@code size} gives a shorter session.
   */
  static List<Question> select(List<Question> bank, int size, Random random, Map<Long, Instant> lastSeen) {
    return select(bank, size, random, lastSeen, Set.of());
  }

  /**
   * As {@link #select(List, int, Random, Map)}, but leaves out every question whose {@link #sameQuestionKey} is in
   * {@code shownToday}: the questions this student already met today in this subject, in any chapter and under any
   * wording. A day therefore never shows the same question twice; once a chapter's bank is used up for the day the
   * session is shorter, or empty.
   */
  static List<Question> select(List<Question> bank, int size, Random random, Map<Long, Instant> lastSeen, Set<String> shownToday) {
    List<Question> shuffled = new ArrayList<>(bank.stream().filter(question -> !shownToday.contains(sameQuestionKey(question))).toList());
    Collections.shuffle(shuffled, random);
    shuffled.sort(Comparator.comparing((Question question) -> lastSeen.getOrDefault(question.getId(), Instant.MIN)));
    if (shuffled.stream().anyMatch(question -> question.getDifficultyLevel() == null)) {
      return distinctQuestions(shuffled, size, lastSeen);
    }
    int easy = (int) Math.ceil(size * 0.3), hard = (int) Math.floor(size * 0.3), medium = size - easy - hard;
    boolean fullTest = size >= FULL_TEST_CHOICES;
    List<Question> essayPool = shuffled.stream().filter(Question::isEssay).toList();
    int essays = Math.min(essayPool.size(), fullTest ? FULL_TEST_ESSAYS : hard > 0 ? 1 : 0);
    List<Question> selected = new ArrayList<>(size + essays);
    Set<String> usedNumbers = new HashSet<>();
    selected.addAll(spreadAcrossForms(ofLevel(shuffled, MathQuestionBank.EASY), easy, lastSeen, usedNumbers));
    selected.addAll(spreadAcrossForms(ofLevel(shuffled, MathQuestionBank.MEDIUM), medium, lastSeen, usedNumbers));
    selected.addAll(spreadAcrossForms(ofLevel(shuffled, MathQuestionBank.HARD), fullTest ? hard : hard - essays, lastSeen, usedNumbers));
    selected.addAll(spreadAcrossForms(essayPool, essays, lastSeen, usedNumbers));
    return selected;
  }

  private static List<Question> ofLevel(List<Question> questions, int level) {
    return questions.stream().filter(question -> !question.isEssay() && question.getDifficultyLevel() == level).toList();
  }

  /**
   * Banks without levels often reword one fact several times, so rewordings of a fact count as the same question.
   * A fact seen recently through any wording waits its turn, and the wording shown longest ago is used.
   */
  private static List<Question> distinctQuestions(List<Question> oldestFirst, int size, Map<Long, Instant> lastSeen) {
    Map<String, Instant> factSeen = new HashMap<>();
    for (Question question : oldestFirst) factSeen.merge(sameQuestionKey(question), lastSeen.getOrDefault(question.getId(), Instant.MIN), (a, b) -> a.isAfter(b) ? a : b);
    List<Question> byFact = new ArrayList<>(oldestFirst);
    byFact.sort(Comparator.comparing((Question question) -> factSeen.get(sameQuestionKey(question))));
    Set<String> used = new HashSet<>();
    return byFact.stream().filter(question -> used.add(sameQuestionKey(question))).limit(size).toList();
  }

  /**
   * What a child would recognise as the same question. Computed math questions are told apart by their prompt. For
   * the other banks a fact is its explanation together with its answer, so rewordings of one fact share a key while
   * different sentences that share a general explanation (such as a punctuation rule) stay distinct.
   */
  static String sameQuestionKey(Question question) {
    if (question.getDifficultyLevel() != null) return "soal:" + normalize(question.getPrompt());
    String[] options = question.getOptions();
    int correct = question.getCorrectOptionIndex() == null ? 0 : question.getCorrectOptionIndex();
    String answer = correct < options.length ? options[correct] : "";
    return "fakta:" + normalize(question.getExplanation()) + "|" + normalize(answer);
  }

  private static String normalize(String text) {
    return text == null ? "" : text.toLowerCase(java.util.Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", " ").strip();
  }

  /** The same numbers asked another way, such as 27 + 49 and ... + 49 = 76, still feels like a repeat. */
  private static String numbersIn(Question question) {
    List<String> numbers = new ArrayList<>();
    Matcher matcher = DIGITS.matcher(question.getPrompt());
    while (matcher.find()) numbers.add(matcher.group());
    Collections.sort(numbers);
    return numbers.isEmpty() ? question.getPrompt() : String.join(" ", numbers);
  }

  /** Takes from whichever form has the least recently seen question, using each form evenly before repeating one. */
  private static List<Question> spreadAcrossForms(List<Question> questions, int count, Map<Long, Instant> lastSeen, Set<String> usedNumbers) {
    List<Question> pool = questions.stream().filter(question -> !usedNumbers.contains(numbersIn(question))).toList();
    Map<String, List<Question>> byForm = new LinkedHashMap<>();
    for (Question question : pool) byForm.computeIfAbsent(String.valueOf(question.getQuestionForm()), key -> new ArrayList<>()).add(question);
    Map<String, Integer> used = new HashMap<>();
    List<Question> picked = new ArrayList<>(count);
    while (picked.size() < count && !byForm.isEmpty()) {
      String form = byForm.keySet().stream().min(Comparator
          .comparing((String key) -> lastSeen.containsKey(byForm.get(key).get(0).getId()))
          .thenComparingInt(key -> used.getOrDefault(key, 0))).orElseThrow();
      Question next = byForm.get(form).remove(0);
      if (byForm.get(form).isEmpty()) byForm.remove(form);
      if (!usedNumbers.add(numbersIn(next))) continue;
      picked.add(next);
      used.merge(form, 1, Integer::sum);
    }
    return picked;
  }
}
