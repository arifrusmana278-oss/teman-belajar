package id.belajarsd.app;

import java.security.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final StudentAccountRepository accounts;
  private final SecureRandom random = new SecureRandom();
  private final Map<String, Long> sessions = new ConcurrentHashMap<>();
  public AuthService(StudentAccountRepository accounts) { this.accounts = accounts; }

  public Map<String, Object> register(String name, String username, String email, String grade, String password) {
    String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
    String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
    if (accounts.existsByEmail(normalizedEmail)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email sudah terdaftar.");
    if (accounts.existsByUsernameIgnoreCase(normalizedUsername)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Username sudah digunakan.");
    StudentAccount account = accounts.save(new StudentAccount(name.trim(), normalizedUsername, normalizedEmail, grade, hash(password)));
    return session(account);
  }
  public Map<String, Object> login(String identifier, String password) {
    String normalized = identifier.trim().toLowerCase(Locale.ROOT);
    StudentAccount account = accounts.findByEmailIgnoreCaseOrUsernameIgnoreCase(normalized, normalized)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email atau kata sandi salah."));
    if (!verify(password, account.getPasswordHash())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email atau kata sandi salah.");
    return session(account);
  }
  public StudentAccount requireSession(String token) {
    Long id = sessions.get(token);
    if (id == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesi sudah berakhir. Silakan masuk kembali.");
    return accounts.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Akun tidak ditemukan."));
  }
  @Transactional
  public String bootstrapAdmin(String email, String configuredPassword) {
    accounts.findByRoleIsNull().forEach(account -> { account.setRole("STUDENT"); if (account.getUsername() == null) account.setUsername(legacyUsername(account)); accounts.save(account); });
    accounts.findByUsernameIsNull().forEach(account -> { account.setUsername(legacyUsername(account)); accounts.save(account); });
    String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
    var existing = accounts.findByEmail(normalizedEmail);
    if (existing.isPresent()) {
      StudentAccount account = existing.get(); account.setRole("ADMIN"); account.setUsername("admin");
      if (configuredPassword != null && !configuredPassword.isBlank()) account.setPasswordHash(hash(configuredPassword));
      accounts.save(account); return null;
    }
    String generatedPassword = configuredPassword == null || configuredPassword.isBlank() ? randomPassword() : null;
    String initialPassword = generatedPassword == null ? configuredPassword : generatedPassword;
    StudentAccount account = new StudentAccount("Administrator", "admin", normalizedEmail, "1", hash(initialPassword), "ADMIN");
    accounts.save(account);
    return generatedPassword;
  }
  public Map<String, Object> updateGrade(String token, String grade) {
    StudentAccount account = requireSession(token);
    account.setGrade(grade);
    return profile(accounts.save(account));
  }
  public void logout(String token) { sessions.remove(token); }
  public Map<String, Object> profile(StudentAccount account) {
    return Map.of("id", account.getId(), "name", account.getName(), "username", account.getUsername() == null ? "" : account.getUsername(), "email", account.getEmail(), "grade", account.getGrade(), "role", account.getRole());
  }
  private String legacyUsername(StudentAccount account) { String base = account.getEmail().split("@", 2)[0].replaceAll("[^a-zA-Z0-9._-]", ""); return (base.isBlank() ? "siswa" : base) + "-" + account.getId(); }
  private String randomPassword() { byte[] bytes = new byte[18]; random.nextBytes(bytes); return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
  private Map<String, Object> session(StudentAccount account) {
    byte[] bytes = new byte[32]; random.nextBytes(bytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    sessions.put(token, account.getId());
    return Map.of("token", token, "user", profile(account));
  }
  private String hash(String password) {
    byte[] salt = new byte[16]; random.nextBytes(salt);
    byte[] derived = derive(password, salt);
    return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(derived);
  }
  private boolean verify(String password, String encoded) {
    try {
      String[] parts = encoded.split(":", 2);
      byte[] expected = Base64.getDecoder().decode(parts[1]);
      return MessageDigest.isEqual(expected, derive(password, Base64.getDecoder().decode(parts[0])));
    } catch (RuntimeException ex) { return false; }
  }
  private byte[] derive(String password, byte[] salt) {
    try { PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 210_000, 256); try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); } finally { spec.clearPassword(); } }
    catch (GeneralSecurityException ex) { throw new IllegalStateException("Tidak dapat memproses kata sandi", ex); }
  }
}
