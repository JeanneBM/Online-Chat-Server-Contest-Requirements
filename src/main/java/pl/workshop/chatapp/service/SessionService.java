package pl.workshop.chatapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.workshop.chatapp.model.ActiveSession;
import pl.workshop.chatapp.model.PresenceStatus;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.ActiveSessionRepository;
import pl.workshop.chatapp.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionService {

    private final ActiveSessionRepository sessionRepo;
    private final UserRepository userRepo;

    public ActiveSession createLoginSession(String email, String ip, String userAgent) {
        User user = userRepo.findByEmail(email).orElseThrow();

        ActiveSession session = new ActiveSession();
        session.setUser(user);
        session.setSessionId(UUID.randomUUID().toString());
        session.setIpAddress(ip);
        session.setUserAgent(userAgent);
        session.setLastActivity(LocalDateTime.now());
        session.setActive(true);

        ActiveSession saved = sessionRepo.save(session);

        user.setLastActivity(LocalDateTime.now());
        user.setPresenceStatus(PresenceStatus.ONLINE);
        userRepo.save(user);

        return saved;
    }

    public void updateActivity(String email, String sessionId, String ip, String userAgent) {
        User user = userRepo.findByEmail(email).orElseThrow();

        ActiveSession session = sessionRepo.findBySessionIdAndUser(sessionId, user)
                .orElseGet(() -> {
                    ActiveSession newSession = new ActiveSession();
                    newSession.setUser(user);
                    newSession.setSessionId(sessionId);
                    return newSession;
                });

        session.setIpAddress(ip);
        session.setUserAgent(userAgent);
        session.setLastActivity(LocalDateTime.now());
        session.setActive(true);
        sessionRepo.save(session);

        user.setLastActivity(LocalDateTime.now());
        user.setPresenceStatus(PresenceStatus.ONLINE);
        userRepo.save(user);
    }

    public boolean isSessionActive(String email, String sessionId) {
        User user = userRepo.findByEmail(email).orElse(null);
        if (user == null) {
            return false;
        }

        return sessionRepo.findBySessionIdAndUser(sessionId, user)
                .map(ActiveSession::isActive)
                .orElse(false);
    }

    public void logoutSession(String email, String sessionId) {
        User user = userRepo.findByEmail(email).orElseThrow();
        ActiveSession session = sessionRepo.findBySessionIdAndUser(sessionId, user).orElseThrow();

        session.setActive(false);
        sessionRepo.save(session);

        updatePresenceFromSessions(user);
    }

    public List<ActiveSession> getActiveSessions(String email) {
        User user = userRepo.findByEmail(email).orElseThrow();
        return sessionRepo.findByUserAndActiveTrue(user);
    }

    private void updatePresenceFromSessions(User user) {
        List<ActiveSession> activeSessions = sessionRepo.findByUserAndActiveTrue(user);

        if (activeSessions.isEmpty()) {
            user.setPresenceStatus(PresenceStatus.OFFLINE);
        } else {
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(1);

            boolean anyRecentlyActive = activeSessions.stream()
                    .anyMatch(s -> s.getLastActivity() != null && s.getLastActivity().isAfter(threshold));

            user.setPresenceStatus(anyRecentlyActive ? PresenceStatus.ONLINE : PresenceStatus.AFK);
            user.setLastActivity(activeSessions.stream()
                    .map(ActiveSession::getLastActivity)
                    .filter(t -> t != null)
                    .max(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now()));
        }

        userRepo.save(user);
    }

    @Scheduled(fixedRate = 1000)
    public void checkAFK() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(1);
        List<User> users = userRepo.findAll();

        for (User user : users) {
            List<ActiveSession> activeSessions = sessionRepo.findByUserAndActiveTrue(user);

            if (activeSessions.isEmpty()) {
                user.setPresenceStatus(PresenceStatus.OFFLINE);
            } else {
                boolean anyRecentlyActive = activeSessions.stream()
                        .anyMatch(s -> s.getLastActivity() != null && s.getLastActivity().isAfter(threshold));
                user.setPresenceStatus(anyRecentlyActive ? PresenceStatus.ONLINE : PresenceStatus.AFK);
            }

            userRepo.save(user);
        }
    }
}
