package id.belajarsd.app;

import jakarta.persistence.*;
import java.time.Instant;

/** Records which question a student was last shown, so later sessions can favour ones they have not seen. */
@Entity
@Table(name = "question_exposures", indexes = @Index(columnList = "studentId, lessonId"))
public class QuestionExposure {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false) private Long studentId;
  @Column(nullable = false) private Long lessonId;
  @Column(nullable = false) private Long questionId;
  @Column(nullable = false) private Instant seenAt;

  protected QuestionExposure() {}

  public QuestionExposure(Long studentId, Long lessonId, Long questionId, Instant seenAt) {
    this.studentId = studentId;
    this.lessonId = lessonId;
    this.questionId = questionId;
    this.seenAt = seenAt;
  }

  public Long getQuestionId() { return questionId; }
  public Instant getSeenAt() { return seenAt; }
  public void seenAgain(Instant when) { seenAt = when; }
}
