package pl.workshop.chatapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import pl.workshop.chatapp.model.PresenceStatus;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PresenceBroadcastListener {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onPresenceStatusChanged(PresenceStatusChangedEvent event) {
        if (event == null || event.username() == null || event.username().isBlank()) {
            return;
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("username", event.username());
        payload.put("status", event.status() != null
                ? event.status().name()
                : PresenceStatus.OFFLINE.name());

        messagingTemplate.convertAndSend("/topic/presence", payload);
    }
}
