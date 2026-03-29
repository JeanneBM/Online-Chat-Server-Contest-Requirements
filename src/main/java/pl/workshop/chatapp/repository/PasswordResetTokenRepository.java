package pl.workshop.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.workshop.chatapp.model.PasswordResetToken;
import pl.workshop.chatapp.model.User;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}
