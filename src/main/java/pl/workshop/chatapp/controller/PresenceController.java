package pl.workshop.chatapp.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import pl.workshop.chatapp.service.SessionService;

import java.security.Principal;
import java.util.Map;

@Controller
public class PresenceController {
    private final SessionService sessionService;

    public PresenceController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @MessageMapping("/presence.ping")
    public void ping(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        if (principal == null) {
            return;
        }

        Map<String, Object> attrs = headerAccessor != null ? headerAccessor.getSessionAttributes() : null;
        Object sessionId = attrs != null ? attrs.get("authSessionId") : null;

        if (sessionId instanceof String s && !s.isBlank()) {
            sessionService.updateWebSocketActivity(principal.getName(), s);
        }
    }
}
