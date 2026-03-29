package pl.workshop.chatapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friends")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user1_id")
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user2_id")
    private User user2;

    private boolean bannedByUser1 = false;
    private boolean bannedByUser2 = false;
}
