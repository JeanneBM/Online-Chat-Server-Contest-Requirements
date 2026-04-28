package pl.workshop.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.workshop.chatapp.model.ActiveSession;

public interface ActiveSessionRepository extends JpaRepository<ActiveSession, Long> {
}
