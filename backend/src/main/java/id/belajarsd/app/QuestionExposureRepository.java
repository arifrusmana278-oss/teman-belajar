package id.belajarsd.app;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionExposureRepository extends JpaRepository<QuestionExposure, Long> {
  List<QuestionExposure> findByStudentIdAndLessonId(Long studentId, Long lessonId);
  List<QuestionExposure> findByStudentIdAndSeenAtGreaterThanEqual(Long studentId, java.time.Instant since);
  void deleteByStudentId(Long studentId);
}
