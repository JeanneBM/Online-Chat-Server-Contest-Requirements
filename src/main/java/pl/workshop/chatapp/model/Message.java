package pl.workshop.chatapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "reply_to_id")
    private Message replyTo;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime editedAt;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    private String attachmentUrl;


    private LocalDateTime readAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Attachment> attachments = new HashSet<>();

    public Long getReplyToId() {
        return replyTo != null ? replyTo.getId() : null;
    }

    public void setReplyToId(Long replyToId) {
        if (replyToId == null) {
            this.replyTo = null;
            return;
        }

        Message parentMessage = new Message();
        parentMessage.setId(replyToId);
        this.replyTo = parentMessage;
    }

    public LocalDateTime getTimestamp() {
        return createdAt;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.createdAt = timestamp;
    }
}
