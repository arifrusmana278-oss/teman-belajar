package id.belajarsd.app;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface QuestionRepository extends JpaRepository<Question,Long> {
  List<Question> findByLessonIdOrderByIdAsc(Long lessonId);
  Optional<Question> findByIdAndLessonId(Long id, Long lessonId);

  @Query("select q from Question q where q.lesson.id = :lessonId and "
      + "(q.expiresAt is null or q.expiresAt > :now) order by q.id")
  List<Question> findActiveByLessonId(@Param("lessonId") Long lessonId, @Param("now") Instant now);

  @Query("select q from Question q where q.lesson.id = :lessonId and (q.source = 'SEED' or q.source is null) order by q.id")
  List<Question> findSeedFallbackByLessonId(@Param("lessonId") Long lessonId);

  default List<Question> findAvailableByLessonId(Long lessonId, Instant now) {
    List<Question> active = findActiveByLessonId(lessonId, now);
    return active.isEmpty() ? findSeedFallbackByLessonId(lessonId) : active;
  }

  @Query("select max(q.createdAt) from Question q where q.lesson.id = :lessonId and q.source = 'AI'")
  Instant findLatestAiQuestionAt(@Param("lessonId") Long lessonId);

  List<Question> findByLessonIdAndSourceAndExpiresAtAfter(Long lessonId, String source, Instant now);

  @Modifying
  @Transactional
  @Query("delete from Question q where q.source = 'AI' and q.expiresAt <= :now")
  int deleteExpiredAiQuestions(@Param("now") Instant now);

  @Modifying
  @Transactional
  @Query("update Question q set q.expiresAt = :expiresAt where q.lesson.id = :lessonId "
      + "and (q.source = 'SEED' or q.source is null) and q.expiresAt is null")
  int setSeedRetention(@Param("lessonId") Long lessonId, @Param("expiresAt") Instant expiresAt);

  @Modifying
  @Transactional
  @Query("delete from Question q where (q.source = 'SEED' or q.source is null) and q.expiresAt <= :now "
      + "and exists (select newer.id from Question newer where newer.lesson = q.lesson and newer.source = 'AI' and newer.expiresAt > :now)")
  int deleteReplacedSeedQuestions(@Param("now") Instant now);

  long countBySource(String source);

  @Query("select max(q.createdAt) from Question q where q.source = 'AI'")
  Instant findLatestAiQuestionAt();
}
