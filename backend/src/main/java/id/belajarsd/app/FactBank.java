package id.belajarsd.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Chapter question banks kept as plain text under {@code resources/question-bank}, so teachers can review and extend
 * them without touching code. Every line is one distinct question; the same fact is never written twice in a
 * chapter. Format:
 *
 * <pre>
 * == Subject | grade | Chapter title        starts a chapter (title without the "Bab N —" prefix)
 * > text                                    material for a chapter of a new subject (optional, may repeat)
 * prompt | answer | wrong | wrong | wrong | explanation
 * %prompt text with {0}                     template for the vocabulary lines that follow ({0} = cue, {1} = answer)
 * %explain text with {0} and {1}
 * %cue-as-wrong                             also offer the cue itself as a wrong option (for kata baku)
 * ~ cue | answer                            one vocabulary question; wrong options come from the chapter's other answers
 * @ Subject | grade | Chapter title         reuse another chapter's questions
 * # comment
 * </pre>
 */
final class FactBank {
  record Chapter(String subject, int grade, String title, String material, List<QuestionBank.Item> items) {}

  private static final List<String> FILES = List.of("fiqih.txt", "akidah-akhlak.txt", "ski.txt", "sejarah-indonesia.txt",
      "pai.txt", "ipas.txt", "bahasa-inggris.txt", "bahasa-arab.txt", "bahasa-indonesia.txt",
      "pancasila.txt", "pjok.txt", "seni.txt");
  private static final Map<String, Chapter> CHAPTERS = load();

  private FactBank() {}

  static List<QuestionBank.Item> forChapter(String subject, int grade, String title) {
    Chapter chapter = CHAPTERS.get(key(subject, grade, stripChapterNumber(title)));
    return chapter == null ? List.of() : chapter.items();
  }

  /** The chapter's written material, or an empty string when the bank file gives none. */
  static String materialOf(String subject, int grade, String title) {
    Chapter chapter = CHAPTERS.get(key(subject, grade, stripChapterNumber(title)));
    return chapter == null ? "" : chapter.material();
  }

  /** Chapters of a subject in file order; used for subjects whose chapter list lives only in these files. */
  static List<Chapter> chaptersOf(String subject) {
    return CHAPTERS.values().stream().filter(chapter -> chapter.subject().equals(subject)).toList();
  }

  static String stripChapterNumber(String title) {
    return title.replaceFirst("(?i)^Bab\\s+\\d+\\s*[—–-]\\s*", "");
  }

  private static String key(String subject, int grade, String title) {
    return subject + "|" + grade + "|" + title;
  }

  private static Map<String, Chapter> load() {
    Map<String, Draft> drafts = new LinkedHashMap<>();
    for (String file : FILES) read(file, drafts);
    Map<String, Chapter> chapters = new LinkedHashMap<>();
    for (Draft draft : drafts.values()) {
      List<QuestionBank.Item> items = new ArrayList<>(draft.items);
      for (String include : draft.includes) {
        Draft source = drafts.get(include);
        if (source == null) throw new IllegalStateException("Bab rujukan tidak ditemukan: " + include);
        if (!source.includes.isEmpty()) throw new IllegalStateException("Rujukan bertingkat tidak didukung: " + include);
        items.addAll(source.items);
      }
      chapters.put(draft.key(), new Chapter(draft.subject, draft.grade, draft.title, String.join(" ", draft.material), List.copyOf(items)));
    }
    return chapters;
  }

  private static final class Draft {
    final String subject, title; final int grade;
    final List<String> material = new ArrayList<>();
    final List<QuestionBank.Item> items = new ArrayList<>();
    final List<String> includes = new ArrayList<>();
    final List<String[]> vocabulary = new ArrayList<>();
    String promptTemplate, explainTemplate; boolean cueAsWrong;
    Draft(String subject, int grade, String title) { this.subject = subject; this.grade = grade; this.title = title; }
    String key() { return FactBank.key(subject, grade, title); }
  }

  private static void read(String file, Map<String, Draft> drafts) {
    InputStream stream = FactBank.class.getResourceAsStream("/question-bank/" + file);
    if (stream == null) throw new IllegalStateException("Berkas bank soal tidak ditemukan: " + file);
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      Draft current = null;
      String line; int number = 0;
      while ((line = reader.readLine()) != null) {
        number++;
        line = line.strip();
        if (line.isEmpty() || line.startsWith("#")) continue;
        String where = file + ":" + number;
        if (line.startsWith("==")) {
          finishVocabulary(current, where);
          String[] head = fields(line.substring(2), 3, where);
          current = new Draft(head[0], Integer.parseInt(head[1]), head[2]);
          if (drafts.putIfAbsent(current.key(), current) != null) throw new IllegalStateException("Bab ganda di " + where);
          continue;
        }
        if (current == null) throw new IllegalStateException("Baris di luar bab di " + where);
        if (line.startsWith(">")) current.material.add(line.substring(1).strip());
        else if (line.startsWith("@")) {
          String[] ref = fields(line.substring(1), 3, where);
          current.includes.add(key(ref[0], Integer.parseInt(ref[1]), ref[2]));
        } else if (line.startsWith("%prompt")) { finishVocabulary(current, where); current.promptTemplate = line.substring(7).strip(); }
        else if (line.startsWith("%explain")) current.explainTemplate = line.substring(8).strip();
        else if (line.startsWith("%cue-as-wrong")) current.cueAsWrong = true;
        else if (line.startsWith("~")) current.vocabulary.add(fields(line.substring(1), 2, where));
        else {
          String[] f = fields(line, 6, where);
          current.items.add(item(f[0], f[1], List.of(f[2], f[3], f[4]), f[5], where));
        }
      }
      finishVocabulary(current, file + ":akhir");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** Turns the pending vocabulary lines into questions; wrong options rotate through the group's other answers. */
  private static void finishVocabulary(Draft draft, String where) {
    if (draft == null || draft.vocabulary.isEmpty()) return;
    if (draft.promptTemplate == null || draft.explainTemplate == null) throw new IllegalStateException("%prompt/%explain belum diisi sebelum " + where);
    List<String[]> words = draft.vocabulary;
    if (words.size() < 4) throw new IllegalStateException("Kelompok kosakata minimal 4 kata sebelum " + where);
    for (int i = 0; i < words.size(); i++) {
      String cue = words.get(i)[0], answer = words.get(i)[1];
      List<String> wrong = new ArrayList<>();
      if (draft.cueAsWrong) wrong.add(cue);
      for (int step : new int[] {1, 2, 3, 4, 5}) {
        String candidate = words.get((i + step) % words.size())[1];
        if (wrong.size() < 3 && !candidate.equals(answer) && !wrong.contains(candidate)) wrong.add(candidate);
      }
      draft.items.add(item(draft.promptTemplate.replace("{0}", cue).replace("{1}", answer), answer, wrong,
          draft.explainTemplate.replace("{0}", cue).replace("{1}", answer), where));
    }
    words.clear();
    draft.cueAsWrong = false;
  }

  private static QuestionBank.Item item(String prompt, String answer, List<String> wrong, String explanation, String where) {
    LinkedHashSet<String> options = new LinkedHashSet<>();
    options.add(answer);
    options.addAll(wrong);
    if (options.size() != 4) throw new IllegalStateException("Pilihan jawaban harus 4 dan berbeda di " + where + ": " + prompt);
    return new QuestionBank.Item(prompt, List.copyOf(options), 0, explanation);
  }

  private static String[] fields(String text, int count, String where) {
    String[] parts = text.split("\\|", -1);
    if (parts.length != count) throw new IllegalStateException("Diharapkan " + count + " kolom di " + where + ": " + text);
    for (int i = 0; i < parts.length; i++) {
      parts[i] = parts[i].strip();
      if (parts[i].isEmpty()) throw new IllegalStateException("Kolom kosong di " + where + ": " + text);
    }
    return parts;
  }
}
