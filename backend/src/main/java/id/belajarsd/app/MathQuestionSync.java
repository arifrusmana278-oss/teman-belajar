package id.belajarsd.app;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Replaces a chapter's stored questions when they differ from its generated math bank. */
@Service
public class MathQuestionSync {
  private final QuestionRepository questions;

  public MathQuestionSync(QuestionRepository questions) { this.questions = questions; }

  /** @return true when this lesson's questions are managed by {@link MathQuestionBank}. */
  @Transactional
  public boolean sync(Lesson lesson) {
    List<MathQuestionBank.MathItem> bank = MathQuestionBank.forLesson(lesson);
    if (bank.isEmpty()) return false;
    List<Question> existing = questions.findByLessonIdOrderByIdAsc(lesson.getId());
    if (matches(existing, bank)) return true;
    // Progress points at the lesson rather than the question, so score history survives this replacement.
    questions.deleteAll(existing);
    questions.saveAll(bank.stream().map(item -> new Question(lesson, item.prompt(), String.join("|||", item.options()),
        item.correctOptionIndex(), item.explanation(), item.essay(), item.level(), item.longMethod(), item.quickMethod(), item.form())).toList());
    return true;
  }

  private static boolean matches(List<Question> existing, List<MathQuestionBank.MathItem> bank) {
    if (existing.size() != bank.size()) return false;
    for (int i = 0; i < bank.size(); i++) {
      Question question = existing.get(i);
      MathQuestionBank.MathItem item = bank.get(i);
      if (!item.prompt().equals(question.getPrompt())
          || !item.options().equals(List.of(question.getOptions()))
          || item.correctOptionIndex() != question.getCorrectOptionIndex()
          || item.essay() != question.isEssay()
          || !item.form().equals(question.getQuestionForm())
          || !item.explanation().equals(question.getExplanation())
          || !Integer.valueOf(item.level()).equals(question.getDifficultyLevel())
          || !item.longMethod().equals(question.getLongMethod())
          || !java.util.Objects.equals(item.quickMethod(), question.getQuickMethod())) return false;
    }
    return true;
  }
}
