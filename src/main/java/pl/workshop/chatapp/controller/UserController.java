package pl.workshop.chatapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.model.UserSession;
import pl.workshop.chatapp.repository.UserRepository;
import pl.workshop.chatapp.service.SessionService;
import pl.workshop.chatapp.service.UserService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final SessionService sessionService;
    private final UserRepository userRepository;

    public UserController(UserService userService, SessionService sessionService, UserRepository userRepository) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.userRepository = userRepository;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        userService.deleteAccount(user.getId());
        return ResponseEntity.ok("Konto usunięte");
    }

    @GetMapping("/sessions")
    public List<UserSession> getSessions(Principal principal) {
        return sessionService.getActiveSessions(principal.getName());
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> logoutSession(@PathVariable String sessionId, Principal principal) {
        sessionService.logoutSession(principal.getName(), sessionId);
        return ResponseEntity.ok("Sesja wylogowana");
    }
}
