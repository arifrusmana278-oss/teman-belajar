package id.belajarsd.app;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationQrRepository extends JpaRepository<DonationQr, Long> {
  Optional<DonationQr> findTopByStatusOrderByUploadedAtDesc(String status);
  Optional<DonationQr> findByApprovalTokenHash(String approvalTokenHash);
}
