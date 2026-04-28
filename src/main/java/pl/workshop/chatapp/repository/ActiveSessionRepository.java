package pl.workshop.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.model.ActiveSession;

import java.util.List;
import java.util.Optional;

public interface ActiveSessionRepository extends JpaRepository<ActiveSession, Long> {

    List<ActiveSession> findByUser(User user);

    List<ActiveSession> findByUserAndActiveTrue(User user);

    Optional<ActiveSession> findBySessionIdAndUser(String sessionId, User user);

    void deleteBySessionId(String sessionId);
}
