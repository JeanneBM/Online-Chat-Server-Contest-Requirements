package pl.workshop.chatapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String sessionId;           // UUID sesji WebSocket / JWT
    private String ipAddress;
    private String userAgent;
    private LocalDateTime lastActivity = LocalDateTime.now();
    private boolean active = true;
}
