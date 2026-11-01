package id.belajarsd.app;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "curriculum_source_snapshots")
public class CurriculumSourceSnapshot {
  @Id
  private String sourceKey;
  @Column(nullable = false, length = 64)
  private String sha256;
  @Column(nullable = false)
  private Instant checkedAt;
  private Instant changedAt;
  @Column(nullable = false)
  private boolean pendingReview;

  protected CurriculumSourceSnapshot() {}

  CurriculumSourceSnapshot(String sourceKey, String sha256, Instant checkedAt) {
    this.sourceKey = sourceKey;
    this.sha256 = sha256;
    this.checkedAt = checkedAt;
  }

  String getSha256() { return sha256; }
  Instant getCheckedAt() { return checkedAt; }
  Instant getChangedAt() { return changedAt; }
  boolean isPendingReview() { return pendingReview; }
  void checked(String newHash, Instant now) {
    if (!sha256.equals(newHash)) {
      sha256 = newHash;
      changedAt = now;
      pendingReview = true;
    }
    checkedAt = now;
  }
  void acknowledge() { pendingReview = false; }
}
