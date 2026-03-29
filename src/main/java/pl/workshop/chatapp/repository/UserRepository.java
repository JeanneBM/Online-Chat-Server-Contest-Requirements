package pl.workshop.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.workshop.chatapp.model.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
}
