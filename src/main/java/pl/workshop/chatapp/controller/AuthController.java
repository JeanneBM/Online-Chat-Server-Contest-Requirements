package pl.workshop.chatapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.model.UserSession;
import pl.workshop.chatapp.repository.UserRepository;
import pl.workshop.chatapp.security.JwtService;
import pl.workshop.chatapp.service.PasswordService;
import pl.workshop.chatapp.service.SessionService;
import pl.workshop.chatapp.service.UserService;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordService passwordService;
    private final SessionService sessionService;
    private final UserService userService;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            PasswordService passwordService,
            SessionService sessionService,
            UserService userService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.passwordService = passwordService;
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String email = request.get("email");
        String username = request.get("username");
        String password = request.get("password");

        email = email != null ? email.trim().toLowerCase() : null;
        username = username != null ? username.trim() : null;

        if (email == null || email.isBlank() || username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body("Email, username i password są wymagane");
        }

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("Email już istnieje");
        }

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body("Username już istnieje");
        }

        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        UserSession session = sessionService.createLoginSession(
                user.getEmail(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        );

        String token = jwtService.generateToken(user.getEmail(), session.getSessionId());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.getUsername(),
                "email", user.getEmail(),
                "sessionId", session.getSessionId()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String email = request.get("email");
        String password = request.get("password");

        email = email != null ? email.trim().toLowerCase() : null;

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body("Email i password są wymagane");
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.badRequest().body("Nieprawidłowy email lub hasło");
        }

        UserSession session = sessionService.createLoginSession(
                user.getEmail(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        );

        String token = jwtService.generateToken(user.getEmail(), session.getSessionId());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.getUsername(),
                "email", user.getEmail(),
                "sessionId", session.getSessionId()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            Principal principal
    ) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Brak zalogowanego użytkownika");
        }

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Brak tokenu Bearer");
        }

        String token = authorization.substring(7);
        String sessionId = jwtService.extractSessionId(token);

        sessionService.logoutSession(principal.getName(), sessionId);

        return ResponseEntity.ok("Wylogowano bieżącą sesję");
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request, Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Brak zalogowanego użytkownika");
        }

        String email = principal.getName();
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (oldPassword == null || oldPassword.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body("oldPassword i newPassword są wymagane");
        }

        User user = userRepository.findByEmail(email).orElseThrow();
        passwordService.changePassword(user.getId(), oldPassword, newPassword);

        return ResponseEntity.ok("Hasło zmienione pomyślnie");
    }

    @PostMapping("/reset-token")
    public ResponseEntity<?> createResetToken(@RequestParam String email) {
        email = email != null ? email.trim().toLowerCase() : null;

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Email jest wymagany");
        }

        String token = passwordService.createResetToken(email);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body("Token i newPassword są wymagane");
        }

        passwordService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Hasło zresetowane pomyślnie");
    }

    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Brak zalogowanego użytkownika");
        }

        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        userService.deleteAccount(user.getId());
        return ResponseEntity.ok("Konto usunięte");
    }
}
