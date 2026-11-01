package id.belajarsd.app;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/donations")
public class DonationController {
  private final DonationService donations;
  private final AuthService auth;
  private final String ownerEmail;

  public DonationController(DonationService donations, AuthService auth,
      @Value("${app.donation.owner-email:arifrusmana278@gmail.com}") String ownerEmail) {
    this.donations = donations; this.auth = auth; this.ownerEmail = ownerEmail;
  }

  @GetMapping("/qr")
  @Transactional(readOnly = true)
  public ResponseEntity<byte[]> approvedQr() {
    DonationQr qr = donations.approvedQr();
    return ResponseEntity.ok().contentType(MediaType.parseMediaType(qr.getContentType()))
        .cacheControl(CacheControl.noCache()).header("X-Content-Type-Options", "nosniff").body(qr.getImageData());
  }

  @GetMapping("/status")
  public Map<String, Object> publicStatus() {
    return Map.of("available", Boolean.TRUE.equals(donations.status().get("approved")));
  }

  @GetMapping("/admin")
  public Map<String, Object> adminStatus(@RequestHeader("Authorization") String authorization) {
    requireAdmin(authorization);
    return donations.status();
  }

  @PostMapping(value = "/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Map<String, Object> upload(@RequestHeader("Authorization") String authorization,
      @RequestPart("image") MultipartFile image, @RequestPart(value = "note", required = false) String note) {
    requireAdmin(authorization);
    try { return donations.upload(image.getContentType(), image.getBytes(), note); }
    catch (java.io.IOException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File gambar tidak dapat dibaca."); }
  }

  @GetMapping(value = "/approve", produces = MediaType.TEXT_HTML_VALUE)
  public String approvalPage(@RequestParam String token) { return donations.approvalPage(token); }

  @PostMapping(value = "/approve", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.TEXT_HTML_VALUE)
  public String approve(@RequestParam String token) {
    donations.approve(token);
    return "<!doctype html><html lang='id'><meta charset='utf-8'><title>QR Donasi Disetujui</title>"
        + "<body style='font:16px system-ui;max-width:560px;margin:12vh auto;padding:24px;color:#263650'><h1>QR donasi aktif</h1>"
        + "<p>QRIS telah disetujui dan sekarang ditampilkan pada dashboard pengguna.</p><p>Persetujuan ini tercatat untuk "
        + ownerEmail + ".</p></body></html>";
  }

  private void requireAdmin(String authorization) {
    if (authorization == null || !authorization.startsWith("Bearer "))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Silakan masuk sebagai admin.");
    if (!"ADMIN".equals(auth.requireSession(authorization.substring(7)).getRole()))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Akses khusus admin.");
  }
}
