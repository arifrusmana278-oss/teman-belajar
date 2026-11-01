package id.belajarsd.app;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExtracurricularRecordRepository extends JpaRepository<ExtracurricularRecord, Long> {
  List<ExtracurricularRecord> findByStudent_Id(Long studentId);
  void deleteByStudent_Id(Long studentId);
}
