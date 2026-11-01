package id.belajarsd.app;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class DonationService {
  private final DonationQrRepository repository;
  private final SecureRandom random = new SecureRandom();
  private final String ownerEmail;
  private final String backendUrl;
  private final String mailFrom;
  private final String mailHost, mailUsername, mailPassword;
  private final int mailPort;
  private final boolean mailAuth, mailStartTls, mailSsl;

  public DonationService(DonationQrRepository repository,
      @Value("${app.donation.owner-email:arifrusmana278@gmail.com}") String ownerEmail,
      @Value("${app.backend-public-url:http://localhost:8080}") String backendUrl,
      @Value("${app.donation.mail.from:}") String mailFrom,
      @Value("${app.donation.mail.host:}") String mailHost,
      @Value("${app.donation.mail.port:587}") int mailPort,
      @Value("${app.donation.mail.username:}") String mailUsername,
      @Value("${app.donation.mail.password:}") String mailPassword,
      @Value("${app.donation.mail.auth:true}") boolean mailAuth,
      @Value("${app.donation.mail.starttls:true}") boolean mailStartTls,
      @Value("${app.donation.mail.ssl:false}") boolean mailSsl) {
    this.repository = repository; this.ownerEmail = ownerEmail;
    this.backendUrl = backendUrl.replaceAll("/$", ""); this.mailFrom = mailFrom;
    this.mailHost = mailHost; this.mailPort = mailPort; this.mailUsername = mailUsername; this.mailPassword = mailPassword;
    this.mailAuth = mailAuth; this.mailStartTls = mailStartTls; this.mailSsl = mailSsl;
  }

  @Transactional
  public Map<String, Object> upload(String contentType, byte[] bytes, String note) {
    if (bytes == null || bytes.length == 0 || bytes.length > 5 * 1024 * 1024)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ukuran gambar harus 1 byte sampai 5 MB.");
    if (!isSupportedImage(contentType, bytes))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File harus berupa gambar QRIS PNG, JPG, atau WebP yang valid.");

    byte[] tokenBytes = new byte[32]; random.nextBytes(tokenBytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    DonationQr pending = repository.save(new DonationQr("PENDING", contentType, cleanNote(note), bytes, hash(token)));
    boolean emailSent = sendApprovalEmail(token, pending);
    return Map.of("id", pending.getId(), "status", pending.getStatus(), "uploadedAt", pending.getUploadedAt().toString(),
        "emailSent", emailSent, "message", emailSent ? "Permintaan persetujuan telah dikirim ke " + ownerEmail + "."
            : "QR tersimpan sebagai menunggu persetujuan. Email belum terkirim; periksa konfigurasi SMTP backend.");
  }

  @Transactional(readOnly = true)
  public Map<String, Object> status() {
    var active = repository.findTopByStatusOrderByUploadedAtDesc("APPROVED");
    var pending = repository.findTopByStatusOrderByUploadedAtDesc("PENDING");
    return Map.of("approved", active.isPresent(), "approvedAt", active.map(q -> q.getApprovedAt().toString()).orElse(""),
        "pending", pending.isPresent(), "pendingAt", pending.map(q -> q.getUploadedAt().toString()).orElse(""),
        "pendingEmail", ownerEmail);
  }

  @Transactional(readOnly = true)
  public DonationQr approvedQr() {
    return repository.findTopByStatusOrderByUploadedAtDesc("APPROVED")
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QR donasi belum tersedia."));
  }

  @Transactional
  public void approve(String token) {
    if (token == null || token.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token persetujuan kosong.");
    DonationQr pending = repository.findByApprovalTokenHash(hash(token))
        .filter(q -> "PENDING".equals(q.getStatus()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.GONE, "Tautan persetujuan tidak valid atau sudah digunakan."));
    repository.findTopByStatusOrderByUploadedAtDesc("APPROVED").ifPresent(DonationQr::archive);
    pending.approve();
  }

  public String approvalPage(String token) {
    if (token == null || token.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token persetujuan kosong.");
    String safeToken = escapeHtml(token);
    return "<!doctype html><html lang='id'><meta charset='utf-8'><meta name='viewport' content='width=device-width'><title>Persetujuan QR Donasi</title>"
        + "<body style='font:16px system-ui;max-width:560px;margin:12vh auto;padding:24px;color:#263650'><h1>Persetujuan QR donasi</h1>"
        + "<p>Admin mengunggah QRIS baru untuk ditampilkan pada dashboard Teman Belajar. Setujui hanya jika Anda mengenali unggahan ini.</p>"
        + "<form method='post' action='/api/donations/approve'><input type='hidden' name='token' value='" + safeToken + "'>"
        + "<button style='padding:12px 18px;border:0;border-radius:8px;background:#4779e8;color:white;font-weight:bold'>Setujui dan aktifkan QR</button></form></body></html>";
  }

  private boolean sendApprovalEmail(String token, DonationQr qr) {
    if (mailHost.isBlank() || mailFrom.isBlank() || (mailAuth && (mailUsername.isBlank() || mailPassword.isBlank()))) return false;
    try (Socket initial = mailSsl ? SSLSocketFactory.getDefault().createSocket(mailHost, mailPort) : new Socket(mailHost, mailPort)) {
      initial.setSoTimeout(12000);
      SmtpSession smtp = new SmtpSession(initial);
      smtp.expect(220); smtp.command("EHLO teman-belajar.local", 250);
      Socket connection = initial;
      if (mailStartTls && !mailSsl) {
        smtp.command("STARTTLS", 220);
        SSLSocket tls = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket(initial, mailHost, mailPort, true);
        tls.setSoTimeout(12000); tls.startHandshake(); connection = tls;
        smtp = new SmtpSession(connection); smtp.command("EHLO teman-belajar.local", 250);
      }
      if (mailAuth) {
        smtp.command("AUTH LOGIN", 334);
        smtp.command(Base64.getEncoder().encodeToString(mailUsername.getBytes(StandardCharsets.UTF_8)), 334);
        smtp.command(Base64.getEncoder().encodeToString(mailPassword.getBytes(StandardCharsets.UTF_8)), 235);
      }
      smtp.command("MAIL FROM:<" + mailFrom + ">", 250);
      smtp.command("RCPT TO:<" + ownerEmail + ">", 250, 251);
      smtp.command("DATA", 354);
      String body = "Admin mengunggah QRIS donasi baru pada " + Instant.now() + ".\nCatatan: " + qr.getNote()
          + "\n\nTinjau dan setujui QR ini melalui tautan berikut:\n" + backendUrl + "/api/donations/approve?token=" + token
          + "\n\nQR baru tidak akan terlihat di dashboard sebelum disetujui.";
      String encodedBody = Base64.getMimeEncoder(76, "\r\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(body.getBytes(StandardCharsets.UTF_8));
      String message = "From: " + mailFrom + "\r\nTo: " + ownerEmail + "\r\nSubject: Approval QRIS donation - Teman Belajar\r\n"
          + "MIME-Version: 1.0\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Transfer-Encoding: base64\r\n\r\n" + encodedBody + "\r\n.";
      smtp.data(message);
      smtp.expect(250); smtp.command("QUIT", 221);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  private static final class SmtpSession {
    private final BufferedReader reader;
    private final BufferedWriter writer;
    SmtpSession(Socket socket) throws IOException {
      reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
      writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }
    void command(String command, int... expected) throws IOException {
      writer.write(command); writer.write("\r\n"); writer.flush(); expect(expected);
    }
    void data(String data) throws IOException { writer.write(data); writer.write("\r\n"); writer.flush(); }
    void expect(int... expected) throws IOException {
      String line; int code = -1;
      do {
        line = reader.readLine();
        if (line == null || line.length() < 3) throw new IOException("Respons SMTP tidak lengkap.");
        try { code = Integer.parseInt(line.substring(0, 3)); }
        catch (NumberFormatException e) { throw new IOException("Respons SMTP tidak valid.", e); }
      } while (line.length() > 3 && line.charAt(3) == '-');
      for (int value : expected) if (code == value) return;
      throw new IOException("Server SMTP menolak perintah dengan kode " + code);
    }
  }

  private String cleanNote(String note) {
    if (note == null || note.isBlank()) return "QRIS donasi";
    String cleaned = note.trim().replaceAll("[\\p{Cntrl}]", " ");
    return cleaned.substring(0, Math.min(cleaned.length(), 500));
  }
  private boolean isSupportedImage(String type, byte[] bytes) {
    if (type == null) return false;
    return (type.equalsIgnoreCase("image/png") && bytes.length > 8 && bytes[0] == (byte) 0x89 && bytes[1] == 0x50)
        || (type.equalsIgnoreCase("image/jpeg") && bytes.length > 3 && bytes[0] == (byte) 0xff && bytes[1] == (byte) 0xd8)
        || (type.equalsIgnoreCase("image/webp") && bytes.length > 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
            && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P');
  }
  private String hash(String token) {
    try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8))); }
    catch (Exception e) { throw new IllegalStateException("SHA-256 tidak tersedia", e); }
  }
  private String escapeHtml(String value) { return value.replace("&", "&amp;").replace("\"", "&quot;").replace("'", "&#39;").replace("<", "&lt;").replace(">", "&gt;"); }
}
