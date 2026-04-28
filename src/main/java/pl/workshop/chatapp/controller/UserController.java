package pl.workshop.chatapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.workshop.chatapp.model.ActiveSession;
import pl.workshop.chatapp.service.SessionService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final SessionService sessionService;

    public UserController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/sessions")
    public List<ActiveSession> getSessions(Principal principal) {
        return sessionService.getActiveSessions(principal.getName());
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> logoutSession(@PathVariable String sessionId, Principal principal) {
        sessionService.logoutSession(principal.getName(), sessionId);
        return ResponseEntity.ok("Sesja wylogowana");
    }
}
