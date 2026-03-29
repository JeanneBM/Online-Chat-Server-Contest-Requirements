package pl.workshop.chatapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.workshop.chatapp.model.UserSession;
import pl.workshop.chatapp.service.SessionService;
import pl.workshop.chatapp.service.UserService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final SessionService sessionService;

    public UserController(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount(Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        userService.deleteAccount(userId);           // usuwa konto + własne pokoje + pliki
        return ResponseEntity.ok("Konto usunięte");
    }

    @GetMapping("/sessions")
    public List<UserSession> getSessions(Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        return sessionService.getActiveSessions(userId);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> logoutSession(@PathVariable String sessionId) {
        sessionService.logoutSession(sessionId);
        return ResponseEntity.ok("Sesja wylogowana");
    }
}
