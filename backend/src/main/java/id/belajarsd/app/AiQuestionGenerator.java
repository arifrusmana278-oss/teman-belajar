package id.belajarsd.app;

import java.util.List;

public interface AiQuestionGenerator {
  List<GeneratedQuestion> generate(Lesson lesson, int count);

  record GeneratedQuestion(String prompt, List<String> options, int correctOptionIndex, String explanation) {}
}
