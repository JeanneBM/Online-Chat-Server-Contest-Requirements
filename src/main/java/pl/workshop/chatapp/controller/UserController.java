package pl.workshop.chatapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.workshop.chatapp.model.ActiveSession;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.UserRepository;
import pl.workshop.chatapp.service.SessionService;
import pl.workshop.chatapp.service.UserService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(SessionService sessionService, UserRepository userRepository, UserService userService) {
        this.sessionService = sessionService;
        this.userRepository = userRepository;
        this.userService = userService;
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

    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Brak zalogowanego użytkownika");
        }

        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        userService.deleteAccount(user.getId());
        return ResponseEntity.ok("Konto usunięte");
    }
}
