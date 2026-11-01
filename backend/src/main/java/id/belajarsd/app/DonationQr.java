package id.belajarsd.app;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "donation_qr")
public class DonationQr {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false, length = 20) private String status;
  @Column(nullable = false, length = 100) private String contentType;
  @Column(nullable = false, length = 500) private String note;
  @Column(nullable = false, columnDefinition = "bytea") private byte[] imageData;
  @Column(length = 64) private String approvalTokenHash;
  @Column(nullable = false) private Instant uploadedAt;
  private Instant approvedAt;

  protected DonationQr() {}
  public DonationQr(String status, String contentType, String note, byte[] imageData, String approvalTokenHash) {
    this.status = status; this.contentType = contentType; this.note = note;
    this.imageData = imageData; this.approvalTokenHash = approvalTokenHash; this.uploadedAt = Instant.now();
  }
  public Long getId() { return id; }
  public String getStatus() { return status; }
  public String getContentType() { return contentType; }
  public String getNote() { return note; }
  public byte[] getImageData() { return imageData; }
  public String getApprovalTokenHash() { return approvalTokenHash; }
  public Instant getUploadedAt() { return uploadedAt; }
  public Instant getApprovedAt() { return approvedAt; }
  public void approve() { status = "APPROVED"; approvalTokenHash = null; approvedAt = Instant.now(); }
  public void archive() { status = "ARCHIVED"; approvalTokenHash = null; }
}
