package pl.workshop.chatapp.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private MessageType type;        // CHAT, JOIN, LEAVE
    private String content;
    private String sender;
    private String roomId;           // wsparcie dla wielu pokojów od razu
    private LocalDateTime timestamp = LocalDateTime.now();
}

public enum MessageType {
    CHAT, JOIN, LEAVE
}
