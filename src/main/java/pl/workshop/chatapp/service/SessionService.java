package pl.workshop.chatapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.workshop.chatapp.model.PresenceStatus;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.model.UserSession;
import pl.workshop.chatapp.repository.UserRepository;
import pl.workshop.chatapp.repository.UserSessionRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserSessionRepository sessionRepo;
    private final UserRepository userRepo;

    public void updateActivity(String username, String sessionId, String ip, String userAgent) {
        User user = userRepo.findByUsername(username).orElseThrow();
        UserSession session = sessionRepo.findByUser(user).stream()
                .filter(s -> s.getSessionId().equals(sessionId))
                .findFirst()
                .orElseGet(() -> {
                    UserSession newSession = new UserSession();
                    newSession.setUser(user);
                    newSession.setSessionId(sessionId);
                    newSession.setIpAddress(ip);
                    newSession.setUserAgent(userAgent);
                    return newSession;
                });
        session.setLastActivity(LocalDateTime.now());
        session.setActive(true);
        sessionRepo.save(session);

        user.setLastActivity(LocalDateTime.now());
        user.setPresenceStatus(PresenceStatus.ONLINE);
        userRepo.save(user);
    }

    public void logoutSession(String sessionId) {
        sessionRepo.deleteBySessionId(sessionId);
    }

    public List<UserSession> getActiveSessions(Long userId) {
        User user = userRepo.findById(userId).orElseThrow();
        return sessionRepo.findByUser(user);
    }

    // AFK po 1 minucie braku aktywności
    @Scheduled(fixedRate = 30000) // co 30 sekund
    public void checkAFK() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(1);
        List<User> users = userRepo.findAll();
        for (User user : users) {
            if (user.getLastActivity().isBefore(threshold)) {
                user.setPresenceStatus(PresenceStatus.AFK);
                userRepo.save(user);
            }
        }
    }
}
