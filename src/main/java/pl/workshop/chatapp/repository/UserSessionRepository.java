package pl.workshop.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.model.UserSession;

import java.util.List;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    List<UserSession> findByUser(User user);
    void deleteBySessionId(String sessionId);
}
