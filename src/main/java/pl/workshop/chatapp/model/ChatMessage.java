package pl.workshop.chatapp.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessage {

    private MessageType type;          // CHAT, JOIN, LEAVE
    private String content;
    private String sender;
    private String attachmentUrl;      // link do pliku/obrazu
    private LocalDateTime timestamp = LocalDateTime.now();
    private String roomId;             // nazwa pokoju (dla pewności routingu)
}

public enum MessageType {
    CHAT, JOIN, LEAVE
}
