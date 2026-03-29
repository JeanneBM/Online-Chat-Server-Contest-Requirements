package pl.workshop.chatapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_bans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserBan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "banner_id", nullable = false)
    private User banner;     // kto zbanował

    @ManyToOne
    @JoinColumn(name = "banned_id", nullable = false)
    private User banned;     // kogo zbanował

    private LocalDateTime bannedAt = LocalDateTime.now();
}
