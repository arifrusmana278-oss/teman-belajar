package id.belajarsd.app;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

class AuthServiceTest {
  private static final String ADMIN_EMAIL = "admin@test.local";
  private StudentAccountRepository accounts;
  private AuthService auth;

  @BeforeEach
  void setUp() {
    accounts = mock(StudentAccountRepository.class);
    auth = new AuthService(accounts, " Admin@Test.local ");
  }

  private StudentAccount admin(String email) {
    return new StudentAccount("Administrator", "admin", email, "1", "old-hash", "ADMIN");
  }

  private StudentAccount student(String username, String email) {
    return new StudentAccount("Siswa", username, email, "3", "student-hash");
  }

  @Test
  void adminWithSameEmailGetsConfiguredPassword() {
    StudentAccount admin = admin(ADMIN_EMAIL);
    when(accounts.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));

    assertNull(auth.bootstrapAdmin(ADMIN_EMAIL, "NewPassword123!"));

    assertNotEquals("old-hash", admin.getPasswordHash());
    verify(accounts).save(admin);
  }

  @Test
  void adminWithSameEmailKeepsPasswordWhenNoneConfigured() {
    StudentAccount admin = admin(ADMIN_EMAIL);
    when(accounts.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));

    assertNull(auth.bootstrapAdmin(ADMIN_EMAIL, ""));

    assertEquals("old-hash", admin.getPasswordHash());
    verify(accounts, never()).save(any());
  }

  @Test
  void adminWithOtherEmailIsKeptUnchanged() {
    StudentAccount admin = admin("lama@test.local");
    when(accounts.findFirstByRoleOrderByIdAsc("ADMIN")).thenReturn(Optional.of(admin));

    assertNull(auth.bootstrapAdmin(ADMIN_EMAIL, "NewPassword123!"));

    assertEquals("old-hash", admin.getPasswordHash());
    assertEquals("lama@test.local", admin.getEmail());
    verify(accounts, never()).save(any());
  }

  @Test
  void studentWithAdminEmailIsNotPromotedWhenAdminExists() {
    StudentAccount student = student("siswa3", ADMIN_EMAIL);
    when(accounts.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(student));
    when(accounts.findFirstByRoleOrderByIdAsc("ADMIN")).thenReturn(Optional.of(admin("lama@test.local")));

    assertNull(auth.bootstrapAdmin(ADMIN_EMAIL, "NewPassword123!"));

    assertEquals("STUDENT", student.getRole());
    assertEquals("student-hash", student.getPasswordHash());
    verify(accounts, never()).save(any());
  }

  @Test
  void studentWithAdminEmailIsNotPromotedWhenNoAdminExists() {
    StudentAccount student = student("siswa3", ADMIN_EMAIL);
    when(accounts.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(student));
    when(accounts.existsByEmail(ADMIN_EMAIL)).thenReturn(true);

    assertNull(auth.bootstrapAdmin(ADMIN_EMAIL, "NewPassword123!"));

    assertEquals("STUDENT", student.getRole());
    assertEquals("student-hash", student.getPasswordHash());
    verify(accounts, never()).save(any());
  }

  @Test
  void takenAdminUsernameDoesNotFailStartupOrCreateAccount() {
    when(accounts.existsByUsernameIgnoreCase("admin")).thenReturn(true);

    assertNull(auth.bootstrapAdmin(ADMIN_EMAIL, "NewPassword123!"));

    verify(accounts, never()).save(any());
  }

  @Test
  void createsAdminWhenNothingExists() {
    String generated = auth.bootstrapAdmin(ADMIN_EMAIL, "");

    assertNotNull(generated);
    ArgumentCaptor<StudentAccount> saved = ArgumentCaptor.forClass(StudentAccount.class);
    verify(accounts).save(saved.capture());
    assertEquals("ADMIN", saved.getValue().getRole());
    assertEquals("admin", saved.getValue().getUsername());
    assertEquals(ADMIN_EMAIL, saved.getValue().getEmail());
  }

  @Test
  void configuredPasswordIsNotReturnedWhenCreatingAdmin() {
    assertNull(auth.bootstrapAdmin(ADMIN_EMAIL, "NewPassword123!"));

    verify(accounts).save(any());
  }

  @Test
  void registerRejectsReservedAdminUsername() {
    var error = assertThrows(ResponseStatusException.class,
        () -> auth.register("Siswa", " ADMIN ", "siswa@test.local", "3", "Password123!"));

    assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    verify(accounts, never()).save(any());
  }

  @Test
  void registerRejectsConfiguredAdminEmail() {
    var error = assertThrows(ResponseStatusException.class,
        () -> auth.register("Siswa", "siswa3", " ADMIN@test.local ", "3", "Password123!"));

    assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    verify(accounts, never()).save(any());
  }

  @Test
  void registerStillAcceptsRegularStudent() {
    when(accounts.save(any())).thenAnswer(call -> {
      StudentAccount account = call.getArgument(0);
      ReflectionTestUtils.setField(account, "id", 1L);
      return account;
    });

    Map<String, Object> session = auth.register("Siswa", "siswa3", "siswa@test.local", "3", "Password123!");

    assertNotNull(session.get("token"));
    verify(accounts).save(any());
  }
}
