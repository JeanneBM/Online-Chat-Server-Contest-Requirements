package pl.workshop.chatapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "room_bans",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "banned_user_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomBan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnoreProperties({"members", "admins", "bannedUsers", "owner"})
    private Room room;

    @ManyToOne(optional = false)
    @JoinColumn(name = "banned_user_id", nullable = false)
    @JsonIgnoreProperties({"friends", "bans", "sessions", "resetTokens", "password", "authorities", "enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "username"})
    private User bannedUser;

    @ManyToOne(optional = false)
    @JoinColumn(name = "banned_by_user_id", nullable = false)
    @JsonIgnoreProperties({"friends", "bans", "sessions", "resetTokens", "password", "authorities", "enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "username"})
    private User bannedByUser;

    @Column(nullable = false)
    private LocalDateTime bannedAt = LocalDateTime.now();
}
