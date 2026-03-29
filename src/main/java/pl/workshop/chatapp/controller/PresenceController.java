package pl.workshop.chatapp.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import pl.workshop.chatapp.service.PresenceService;

@Controller
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @MessageMapping("/presence.ping")
    public void ping(SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        presenceService.updateActivity(username);
    }
}
