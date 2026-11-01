package id.belajarsd.app;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ai_question_scheduler_state")
public class AiQuestionSchedulerState {
  @Id private Long id;
  private Instant retryAfter;
  private Instant lastFailureAt;
  private Instant runStartedAt;
  @Column(length = 1000) private String lastError;

  protected AiQuestionSchedulerState() {}
  AiQuestionSchedulerState(Long id) { this.id = id; }
  public Long getId() { return id; }
  public Instant getRetryAfter() { return retryAfter; }
  public Instant getLastFailureAt() { return lastFailureAt; }
  public Instant getRunStartedAt() { return runStartedAt; }
  public String getLastError() { return lastError; }
  void failed(Instant at, Instant nextRetry, String error) { lastFailureAt = at; retryAfter = nextRetry; lastError = error == null ? "Gagal membuat soal AI." : error.substring(0, Math.min(error.length(), 1000)); }
  void succeeded() { retryAfter = null; lastError = null; }
  void runStarted(Instant at) { runStartedAt = at; }
}
