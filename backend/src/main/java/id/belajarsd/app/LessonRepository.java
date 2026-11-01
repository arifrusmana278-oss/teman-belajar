package id.belajarsd.app;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson,Long>{
 List<Lesson> findBySubjectIdOrderByIdAsc(Long subjectId);

 @Query("select l from Lesson l join fetch l.subject order by l.id")
 List<Lesson> findAllWithSubject();
 boolean existsBySubjectIdAndTitleAndGrade(Long subjectId, String title, String grade);
 Optional<Lesson> findBySubjectIdAndTitleAndGrade(Long subjectId, String title, String grade);

 @Query("select l from Lesson l join fetch l.subject where l.subject.id = :subjectId order by l.id")
 List<Lesson> findBySubjectIdWithSubject(@Param("subjectId") Long subjectId);

 @Query("select l from Lesson l join fetch l.subject where l.id = :lessonId")
 Optional<Lesson> findByIdWithSubject(@Param("lessonId") Long lessonId);
}
