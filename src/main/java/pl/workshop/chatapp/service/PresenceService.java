package pl.workshop.chatapp.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.workshop.chatapp.model.PresenceStatus;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {

    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, LocalDateTime> lastActivityMap = new ConcurrentHashMap<>();

    public PresenceService(UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void userConnected(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setPresenceStatus(PresenceStatus.ONLINE);
            user.setLastActivity(LocalDateTime.now());
            userRepository.save(user);
            broadcastPresence(user);
        });
    }

    public void updateActivity(String username) {
        lastActivityMap.put(username, LocalDateTime.now());
    }

    public void userDisconnected(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setPresenceStatus(PresenceStatus.OFFLINE);
            userRepository.save(user);
            broadcastPresence(user);
        });
    }

    @Scheduled(fixedRate = 30000) // co 30 sekund sprawdzamy AFK
    public void checkAfkStatus() {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        lastActivityMap.forEach((username, time) -> {
            if (time.isBefore(oneMinuteAgo)) {
                userRepository.findByUsername(username).ifPresent(user -> {
                    if (user.getPresenceStatus() != PresenceStatus.AFK) {
                        user.setPresenceStatus(PresenceStatus.AFK);
                        userRepository.save(user);
                        broadcastPresence(user);
                    }
                });
            }
        });
    }

    private void broadcastPresence(User user) {
        messagingTemplate.convertAndSend("/topic/presence", 
            Map.of("username", user.getUsername(), "status", user.getPresenceStatus()));
    }
}
