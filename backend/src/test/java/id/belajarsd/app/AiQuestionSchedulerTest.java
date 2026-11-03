package id.belajarsd.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class AiQuestionSchedulerTest {
  private static Lesson chapter(String subjectName) {
    Subject subject = mock(Subject.class);
    when(subject.getName()).thenReturn(subjectName);
    Lesson lesson = mock(Lesson.class);
    when(lesson.getSubject()).thenReturn(subject);
    when(lesson.getTitle()).thenReturn("Bab 1 — Contoh");
    return lesson;
  }

  @Test
  void mathChaptersStayOutOfTheAiRotation() {
    LessonRepository lessons = mock(LessonRepository.class);
    List<Lesson> catalog = List.of(chapter("Matematika"), chapter("Hitung Cepat"), chapter("Mathematics in English"), chapter("IPAS"), chapter("PAI"));
    when(lessons.findAllWithSubject()).thenReturn(catalog);
    AiQuestionSchedulerStateRepository states = mock(AiQuestionSchedulerStateRepository.class);
    when(states.findById(1L)).thenReturn(Optional.empty());
    AiQuestionScheduler scheduler = new AiQuestionScheduler(new AiQuestionProperties(), mock(AiQuestionGenerator.class), lessons, mock(QuestionRepository.class), states, mock(JdbcTemplate.class));

    assertEquals(2L, scheduler.status().get("chapterCount"));
  }
}
