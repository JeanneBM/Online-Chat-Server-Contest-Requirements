package pl.workshop.chatapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "friend_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(length = 500)
    private String message;                     // opcjonalny tekst zaproszenia

    @Enumerated(EnumType.STRING)
    private FriendRequestStatus status = FriendRequestStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum FriendRequestStatus {
        PENDING, ACCEPTED, REJECTED
    }
}
