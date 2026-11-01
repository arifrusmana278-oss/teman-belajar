package id.belajarsd.app;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "extracurricular_records", uniqueConstraints =
    @UniqueConstraint(columnNames = {"student_id", "record_key"}))
public class ExtracurricularRecord {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional = false) @JoinColumn(name = "student_id", nullable = false) private StudentAccount student;
  @Column(name = "record_key", nullable = false, length = 100) private String recordKey;
  @Column(name = "record_type", nullable = false, length = 20) private String recordType;
  @Column(name = "completed_at") private Instant completedAt;

  protected ExtracurricularRecord() {}

  public ExtracurricularRecord(StudentAccount student, String recordKey, String recordType, Instant completedAt) {
    this.student = student;
    this.recordKey = recordKey;
    this.recordType = recordType;
    this.completedAt = completedAt;
  }

  public String getRecordKey() { return recordKey; }
  public String getRecordType() { return recordType; }
  public Instant getCompletedAt() { return completedAt; }
}
