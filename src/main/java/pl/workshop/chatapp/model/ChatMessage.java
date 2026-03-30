package pl.workshop.chatapp.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private MessageType type = MessageType.CHAT;
    private String content;
    private String sender;
    private String attachmentUrl;
    private LocalDateTime timestamp = LocalDateTime.now();
    private String roomId;
    private Long replyToId;
}
