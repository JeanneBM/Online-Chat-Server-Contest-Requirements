package pl.workshop.chatapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.workshop.chatapp.model.PresenceStatus;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional
public class PresenceService {

    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, LocalDateTime> lastActivityMap = new ConcurrentHashMap<>();

    public void userConnected(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        userRepository.findByEmail(email.trim().toLowerCase()).ifPresent(user -> {
            LocalDateTime now = LocalDateTime.now();
            user.setPresenceStatus(PresenceStatus.ONLINE);
            user.setLastActivity(now);
            lastActivityMap.put(user.getEmail(), now);
            userRepository.save(user);
            broadcastPresence(user);
        });
    }

    public void updateActivity(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();
        LocalDateTime now = LocalDateTime.now();
        lastActivityMap.put(normalizedEmail, now);

        userRepository.findByEmail(normalizedEmail).ifPresent(user -> {
            user.setLastActivity(now);
            if (user.getPresenceStatus() != PresenceStatus.ONLINE) {
                user.setPresenceStatus(PresenceStatus.ONLINE);
            }
            userRepository.save(user);
            broadcastPresence(user);
        });
    }

    public void userDisconnected(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();
        lastActivityMap.remove(normalizedEmail);

        userRepository.findByEmail(normalizedEmail).ifPresent(user -> {
            user.setPresenceStatus(PresenceStatus.OFFLINE);
            userRepository.save(user);
            broadcastPresence(user);
        });
    }

    @Scheduled(fixedRate = 30000)
    public void checkAfkStatus() {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);

        lastActivityMap.forEach((email, activityTime) -> {
            userRepository.findByEmail(email).ifPresent(user -> {
                PresenceStatus newStatus = activityTime.isBefore(oneMinuteAgo)
                        ? PresenceStatus.AFK
                        : PresenceStatus.ONLINE;

                if (user.getPresenceStatus() != newStatus) {
                    user.setPresenceStatus(newStatus);
                    userRepository.save(user);
                    broadcastPresence(user);
                }
            });
        });
    }

    @Transactional(readOnly = true)
    public Map<String, String> getPresenceForContacts(User user) {
        Map<String, String> result = new HashMap<>();

        if (user.getFriends() == null) {
            return result;
        }

        for (User friend : user.getFriends()) {
            PresenceStatus status = friend.getPresenceStatus() != null
                    ? friend.getPresenceStatus()
                    : PresenceStatus.OFFLINE;
            result.put(friend.getUsername(), status.name());
        }

        return result;
    }

    private void broadcastPresence(User user) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", user.getUsername());
        payload.put("status", user.getPresenceStatus() != null
                ? user.getPresenceStatus().name()
                : PresenceStatus.OFFLINE.name());

        messagingTemplate.convertAndSend("/topic/presence", payload);
    }
}
