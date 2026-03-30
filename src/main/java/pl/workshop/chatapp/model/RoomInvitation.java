package pl.workshop.chatapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter;

    @ManyToOne
    @JoinColumn(name = "invitee_id", nullable = false)
    private User invitee;

    private LocalDateTime createdAt = LocalDateTime.now();
    private boolean accepted = false;
    private boolean rejected = false;
    private LocalDateTime respondedAt;
}
