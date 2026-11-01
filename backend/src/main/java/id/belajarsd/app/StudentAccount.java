package id.belajarsd.app;

import jakarta.persistence.*;

@Entity
@Table(name = "student_accounts", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email"), @UniqueConstraint(columnNames = "username")
})
public class StudentAccount {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false, length = 120) private String name;
  @Column(nullable = false, length = 190) private String email;
  @Column(length = 50) private String username;
  @Column(nullable = false, length = 32) private String grade;
  @Column(nullable = false, length = 300) private String passwordHash;
  @Column(length = 20) private String role;
  protected StudentAccount() {}
  public StudentAccount(String name, String username, String email, String grade, String passwordHash) {
    this(name, username, email, grade, passwordHash, "STUDENT");
  }
  public StudentAccount(String name, String username, String email, String grade, String passwordHash, String role) {
    this.name = name; this.username = username; this.email = email; this.grade = grade; this.passwordHash = passwordHash; this.role = role;
  }
  public Long getId() { return id; }
  public String getName() { return name; }
  public String getEmail() { return email; }
  public String getUsername() { return username; }
  public String getGrade() { return grade; }
  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
  public void setGrade(String grade) { this.grade = grade; }
  public void setUsername(String username) { this.username = username; }
  public String getRole() { return role == null ? "STUDENT" : role; }
  public void setRole(String role) { this.role = role; }
}
