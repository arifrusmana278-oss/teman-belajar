package id.belajarsd.app;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService auth;
  public AuthController(AuthService auth) { this.auth = auth; }
  @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
    return auth.register(request.name(), request.username(), request.email(), request.grade(), request.password());
  }
  @PostMapping("/login")
  public Map<String, Object> login(@Valid @RequestBody LoginRequest request) { return auth.login(request.identifier(), request.password()); }
  @GetMapping("/me")
  public Map<String, Object> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
    return auth.profile(auth.requireSession(token(authorization)));
  }
  @PutMapping("/me/grade")
  public Map<String, Object> updateGrade(@RequestHeader(value = "Authorization", required = false) String authorization,
      @Valid @RequestBody GradeRequest request) { return auth.updateGrade(token(authorization), request.grade()); }
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, String>> handleAuthError(ResponseStatusException exception) {
    return ResponseEntity.status(exception.getStatusCode()).body(Map.of("message", exception.getReason() == null ? "Permintaan autentikasi gagal." : exception.getReason()));
  }
  @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(@RequestHeader(value = "Authorization", required = false) String authorization) { auth.logout(token(authorization)); }
  private String token(String authorization) {
    if (authorization == null || !authorization.startsWith("Bearer ")) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Silakan masuk terlebih dahulu.");
    return authorization.substring(7);
  }
  public record RegisterRequest(@NotBlank @Size(max=120) String name, @NotBlank @Pattern(regexp="[a-zA-Z0-9._-]{3,50}", message="Username 3–50 karakter dan hanya boleh memakai huruf, angka, titik, garis bawah, atau tanda hubung.") String username, @NotBlank @Email @Size(max=190) String email,
      @NotBlank @Pattern(regexp="[1-6]", message="Pilih kelas 1 sampai 6.") String grade,
      @NotBlank @Size(min=8,max=100) String password) {}
  public record LoginRequest(@NotBlank @Size(max=190) String identifier, @NotBlank String password) {}
  public record GradeRequest(@NotBlank @Pattern(regexp="[1-6]", message="Pilih kelas 1 sampai 6.") String grade) {}
}
