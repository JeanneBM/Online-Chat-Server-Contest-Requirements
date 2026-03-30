package pl.workshop.chatapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.RoomRepository;
import pl.workshop.chatapp.repository.UserRepository;
import pl.workshop.chatapp.security.JwtService;
import pl.workshop.chatapp.service.PasswordService;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordService passwordService;

    public AuthController(UserRepository userRepository,
                          RoomRepository roomRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          PasswordService passwordService) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.passwordService = passwordService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String username = request.get("username");
        String password = request.get("password");

        email = email != null ? email.trim() : null;
        username = username != null ? username.trim() : null;

        if (email == null || email.isBlank()
                || username == null || username.isBlank()
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

        String token = jwtService.generateToken(user.getUsername());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.getUsername(),
                "email", user.getEmail()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        email = email != null ? email.trim() : null;

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body("Email i password są wymagane");
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.badRequest().body("Nieprawidłowy email lub hasło");
        }

        String token = jwtService.generateToken(user.getUsername());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.getUsername(),
                "email", user.getEmail()
        ));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request, Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Brak zalogowanego użytkownika");
        }

        String username = principal.getName();
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (oldPassword == null || oldPassword.isBlank()
                || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body("oldPassword i newPassword są wymagane");
        }

        User user = userRepository.findByUsername(username).orElseThrow();
        passwordService.changePassword(user.getId(), oldPassword, newPassword);

        return ResponseEntity.ok("Hasło zmienione pomyślnie");
    }

    @PostMapping("/reset-token")
    public ResponseEntity<?> createResetToken(@RequestParam String email) {
        email = email != null ? email.trim() : null;

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

        if (token == null || token.isBlank()
                || newPassword == null || newPassword.isBlank()) {
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

        User user = userRepository.findByUsername(principal.getName()).orElseThrow();

        roomRepository.findAll().forEach(room -> {
            room.getMembers().remove(user);
            room.getAdmins().remove(user);
        });

        roomRepository.findAll().stream()
                .filter(room -> room.getOwner() != null && room.getOwner().equals(user))
                .forEach(roomRepository::delete);

        userRepository.delete(user);

        return ResponseEntity.ok("Konto usunięte");
    }
}
