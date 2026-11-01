package id.belajarsd.app;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentAccountRepository extends JpaRepository<StudentAccount, Long> {
  Optional<StudentAccount> findByEmail(String email);
  Optional<StudentAccount> findByEmailIgnoreCaseOrUsernameIgnoreCase(String email, String username);
  boolean existsByEmail(String email);
  boolean existsByUsernameIgnoreCase(String username);
  List<StudentAccount> findByRoleOrderByIdAsc(String role);
  Optional<StudentAccount> findFirstByRoleOrderByIdAsc(String role);
  List<StudentAccount> findByRoleIsNull();
  List<StudentAccount> findByUsernameIsNull();
}
