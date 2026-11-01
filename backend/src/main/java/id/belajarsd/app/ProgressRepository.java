package id.belajarsd.app;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProgressRepository extends JpaRepository<Progress,Long>{
 @EntityGraph(attributePaths = {"lesson", "lesson.subject"})
 List<Progress> findTop20ByStudentNameOrderByCompletedAtDesc(String studentName);

 @EntityGraph(attributePaths = {"lesson", "lesson.subject"})
 List<Progress> findByStudentName(String studentName);

 void deleteByStudentName(String studentName);

}
