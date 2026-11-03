package id.belajarsd.app;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AiQuestionScheduler {
  private static final Logger log = LoggerFactory.getLogger(AiQuestionScheduler.class);
  private static final Set<String> COMPUTED_SUBJECTS = Set.of("Matematika", "Hitung Cepat", "Mathematics in English");
  private final AiQuestionProperties properties;
  private final AiQuestionGenerator generator;
  private final LessonRepository lessons;
  private final QuestionRepository questions;
  private final AiQuestionSchedulerStateRepository states;
  private final JdbcTemplate jdbc;
  private final AtomicBoolean running = new AtomicBoolean(false);

  public AiQuestionScheduler(AiQuestionProperties properties, AiQuestionGenerator generator,
      LessonRepository lessons, QuestionRepository questions, AiQuestionSchedulerStateRepository states, JdbcTemplate jdbc) {
    this.properties = properties;
    this.generator = generator;
    this.lessons = lessons;
    this.questions = questions;
    this.states = states;
    this.jdbc = jdbc;
  }

  @Scheduled(cron = "${app.ai.schedule-cron:0 59 23 * * SUN}", zone = "${app.ai.schedule-zone:Asia/Jakarta}")
  public void generateDueQuestionSets() {
    if (!properties.isEnabled()) return;
    if (properties.getApiKey() == null || properties.getApiKey().isBlank()
        || properties.getModel() == null || properties.getModel().isBlank()) {
      log.warn("Putaran soal AI dilewati: AI_API_KEY atau AI_MODEL belum dikonfigurasi.");
      return;
    }
    if (!running.compareAndSet(false, true)) return;
    try {
      jdbc.execute((ConnectionCallback<Void>) connection -> {
        boolean acquired;
        try (var statement = connection.prepareStatement("select pg_try_advisory_lock(719204361)")) {
          try (var result = statement.executeQuery()) { result.next(); acquired = result.getBoolean(1); }
        }
        if (!acquired) return null;
        try { runBatch(); }
        finally {
          try (var statement = connection.prepareStatement("select pg_advisory_unlock(719204361)")) { statement.execute(); }
        }
        return null;
      });
    } finally {
      running.set(false);
    }
  }

  @Scheduled(cron = "${app.ai.cleanup-cron:0 15 2 * * *}")
  public void removeExpiredQuestionSets() {
    int deleted = questions.deleteExpiredAiQuestions(Instant.now());
    int replacedSeeds = questions.deleteReplacedSeedQuestions(Instant.now());
    if (deleted + replacedSeeds > 0) log.info("Menghapus {} soal AI kedaluwarsa dan {} soal awal yang telah tergantikan (retensi {} hari).", deleted, replacedSeeds, properties.getRetentionDays());
  }

  public Map<String, Object> status() {
    long chapterCount = lessons.findAllWithSubject().stream().filter(this::isAiEligible).count();
    Instant latestGenerated = questions.findLatestAiQuestionAt();
    AiQuestionSchedulerState state = states.findById(1L).orElse(null);
    Map<String, Object> result = new java.util.LinkedHashMap<>();
    result.put("enabled", properties.isEnabled());
    result.put("configured", properties.getApiKey() != null && !properties.getApiKey().isBlank()
        && properties.getModel() != null && !properties.getModel().isBlank());
    result.put("model", properties.getModel() == null ? "" : properties.getModel());
    result.put("schedule", properties.getScheduleCron()); result.put("scheduleZone", properties.getScheduleZone());
    result.put("retentionDays", properties.getRetentionDays());
    result.put("batchSize", properties.getBatchSize()); result.put("chapterCount", chapterCount);
    result.put("generatedQuestionCount", questions.countBySource("AI"));
    result.put("lastGeneratedAt", latestGenerated == null ? "" : latestGenerated.toString());
    result.put("temporarilySkippedUntil", state == null || state.getRetryAfter() == null ? "" : state.getRetryAfter().toString());
    result.put("lastFailureAt", state == null || state.getLastFailureAt() == null ? "" : state.getLastFailureAt().toString());
    result.put("lastError", state == null || state.getLastError() == null ? "" : state.getLastError());
    return result;
  }

  private void runBatch() {
    Instant now = Instant.now();
    int limit = Math.max(1, properties.getBatchSize());
    int attempted = 0, succeeded = 0;
    AiQuestionSchedulerState schedulerState = states.findById(1L).orElseGet(() -> new AiQuestionSchedulerState(1L));
    schedulerState.runStarted(now);
    states.save(schedulerState);
    List<Lesson> catalog = lessons.findAllWithSubject();
    for (Lesson lesson : catalog) {
      if (attempted >= limit) break;
      if (!isAiEligible(lesson)) continue;
      Instant latestGenerated = questions.findLatestAiQuestionAt(lesson.getId());
      if (latestGenerated != null && latestGenerated.isAfter(now)) continue;
      attempted++;
      try {
        List<AiQuestionGenerator.GeneratedQuestion> generated = generator.generate(lesson, properties.getQuestionsPerLesson());
        ensureNoRecentDuplicates(lesson, generated, now);
        Instant expiresAt = now.plus(Math.max(1, properties.getRetentionDays()), ChronoUnit.DAYS);
        List<Question> rows = new ArrayList<>(generated.size());
        for (AiQuestionGenerator.GeneratedQuestion item : generated) {
          rows.add(new Question(lesson, item.prompt(), String.join("|||", item.options()),
              item.correctOptionIndex(), item.explanation(), "AI", expiresAt));
        }
        questions.saveAll(rows);
        questions.setSeedRetention(lesson.getId(), expiresAt);
        succeeded++;
        log.info("Menyimpan {} soal AI untuk bab {} kelas {} ({}).", rows.size(), lesson.getTitle(), lesson.getGrade(), lesson.getSubject().getName());
      } catch (Exception exception) {
        Instant failedAt = Instant.now();
        Instant retryAfter = nextScheduledRun(failedAt);
        AiQuestionSchedulerState state = states.findById(1L).orElseGet(() -> new AiQuestionSchedulerState(1L));
        state.failed(failedAt, retryAfter, exception.getMessage());
        states.save(state);
        log.error("Putaran soal AI dilewati setelah gagal pada lesson id {} ({} · {}): {}. Coba lagi setelah {}.",
            lesson.getId(), lesson.getSubject().getName(), lesson.getTitle(), exception.getMessage(), retryAfter);
        return;
      }
    }
    if (attempted > 0) log.info("Batch soal AI selesai: {} bab dicoba, {} berhasil.", attempted, succeeded);
    if (succeeded > 0) states.findById(1L).ifPresent(state -> { state.succeeded(); states.save(state); });
  }

  private Instant nextScheduledRun(Instant instant) {
    ZonedDateTime next = instant.atZone(ZoneId.of(properties.getScheduleZone()))
        .with(DayOfWeek.SUNDAY).withHour(23).withMinute(59).withSecond(0).withNano(0);
    if (!next.toInstant().isAfter(instant)) next = next.plusWeeks(1);
    return next.toInstant();
  }

  private void ensureNoRecentDuplicates(Lesson lesson, List<AiQuestionGenerator.GeneratedQuestion> generated, Instant now) {
    Set<String> prompts = new HashSet<>();
    for (Question existing : questions.findByLessonIdAndSourceAndExpiresAtAfter(lesson.getId(), "AI", now)) {
      prompts.add(normalize(existing.getPrompt()));
    }
    for (AiQuestionGenerator.GeneratedQuestion item : generated) {
      if (!prompts.add(normalize(item.prompt()))) throw new IllegalStateException("AI mengulang soal yang masih tersimpan.");
    }
  }

  private String normalize(String value) {
    return value.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]", "");
  }

  /** AI output is only format-checked, so math chapters, whose answers must be computed, stay out of the rotation. */
  private boolean isAiEligible(Lesson lesson) {
    return isCatalogChapter(lesson.getTitle()) && !COMPUTED_SUBJECTS.contains(lesson.getSubject().getName());
  }

  private boolean isCatalogChapter(String title) {
    return title != null && title.matches("(?i)^Bab\\s+\\d+\\s*[—–-].*");
  }
}
