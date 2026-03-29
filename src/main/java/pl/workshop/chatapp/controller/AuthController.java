package pl.workshop.chatapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.UserRepository;
import pl.workshop.chatapp.security.JwtService;
import pl.workshop.chatapp.service.PasswordService;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordService passwordService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.passwordService = passwordService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String username = request.get("username");
        String password = request.get("password");

        if (userRepository.existsByEmail(email) || userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body("Email lub username już istnieje");
        }

        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);
        String token = jwtService.generateToken(username);

        return ResponseEntity.ok(Map.of("token", token, "username", username));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.badRequest().body("Nieprawidłowe dane");
        }

        String token = jwtService.generateToken(username);
        return ResponseEntity.ok(Map.of("token", token, "username", username));
    }

    // === PASSWORD MANAGEMENT ===
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request, Principal principal) {
        Long userId = Long.valueOf(principal.getName()); // zakładamy że principal.getName() = userId
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        passwordService.changePassword(userId, oldPassword, newPassword);
        return ResponseEntity.ok("Hasło zmienione pomyślnie");
    }

    @PostMapping("/reset-token")
    public ResponseEntity<?> createResetToken(@RequestParam String email) {
        String token = passwordService.createResetToken(email);
        return ResponseEntity.ok(Map.of("token", token)); // w produkcji wyślij mailem
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        passwordService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Hasło zresetowane pomyślnie");
    }
}
