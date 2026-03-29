package pl.workshop.chatapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.workshop.chatapp.model.PasswordResetToken;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.PasswordResetTokenRepository;
import pl.workshop.chatapp.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Stare hasło nieprawidłowe");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public String createResetToken(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        String token = UUID.randomUUID().toString();
        PasswordResetToken reset = new PasswordResetToken();
        reset.setToken(token);
        reset.setUser(user);
        tokenRepository.save(reset);
        return token; // w realnej apce wysłać mailem – tutaj zwracamy do UI
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken reset = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Nieprawidłowy token"));
        if (reset.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Token wygasł");
        }
        User user = reset.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(reset);
    }
}
