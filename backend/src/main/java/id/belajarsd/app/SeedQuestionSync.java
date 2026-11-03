package id.belajarsd.app;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Keeps a non-math chapter's starter questions equal to its bank. When the bank changes, the old starter rows are
 * replaced; AI questions are left alone. Progress points at the lesson, so score history survives.
 */
@Service
public class SeedQuestionSync {
  private final QuestionRepository questions;

  public SeedQuestionSync(QuestionRepository questions) { this.questions = questions; }

  @Transactional
  public void sync(Lesson lesson, List<QuestionBank.Item> bank) {
    if (bank.isEmpty()) return;
    List<Question> existing = questions.findSeedByLessonId(lesson.getId());
    if (matches(existing, bank)) return;
    questions.deleteAll(existing);
    questions.saveAll(bank.stream().map(item -> new Question(lesson, item.prompt(), String.join("|||", item.options()),
        item.correctOptionIndex(), item.explanation())).toList());
  }

  private static boolean matches(List<Question> existing, List<QuestionBank.Item> bank) {
    if (existing.size() != bank.size()) return false;
    for (int i = 0; i < bank.size(); i++) {
      Question question = existing.get(i);
      QuestionBank.Item item = bank.get(i);
      if (!item.prompt().equals(question.getPrompt()) || !item.options().equals(List.of(question.getOptions()))
          || item.correctOptionIndex() != question.getCorrectOptionIndex() || !item.explanation().equals(question.getExplanation())
          || question.getExpiresAt() != null) return false;
    }
    return true;
  }
}
