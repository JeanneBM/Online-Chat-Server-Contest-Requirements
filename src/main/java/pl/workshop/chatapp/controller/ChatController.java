package pl.workshop.chatapp.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendTo;
import org.springframework.stereotype.Controller;
import pl.workshop.chatapp.model.ChatMessage;
import pl.workshop.chatapp.model.Message;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.MessageRepository;
import pl.workshop.chatapp.repository.RoomRepository;
import pl.workshop.chatapp.repository.UserRepository;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public ChatController(MessageRepository messageRepository,
                          RoomRepository roomRepository,
                          UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    /**
     * Wysyłanie wiadomości do pokoju (tekst + załącznik)
     */
    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable String roomId,
                                   @Payload ChatMessage chatMessage,
                                   SimpMessageHeaderAccessor headerAccessor) {

        String username = headerAccessor.getUser().getName();

        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Room room = roomRepository.findByName(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Zapis do bazy danych
        Message dbMessage = new Message();
        dbMessage.setRoom(room);
        dbMessage.setSender(sender);
        dbMessage.setContent(chatMessage.getContent());
        dbMessage.setAttachmentUrl(chatMessage.getAttachmentUrl());
        dbMessage.setTimestamp(LocalDateTime.now());
        messageRepository.save(dbMessage);

        // Zwrot do wszystkich w pokoju
        chatMessage.setSender(username);
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setRoomId(roomId);

        return chatMessage;
    }

    /**
     * Dołączanie użytkownika do pokoju (JOIN)
     */
    @MessageMapping("/chat.join/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage joinRoom(@DestinationVariable String roomId,
                                SimpMessageHeaderAccessor headerAccessor) {

        String username = headerAccessor.getUser().getName();

        ChatMessage joinMessage = new ChatMessage();
        joinMessage.setType(ChatMessage.MessageType.JOIN);
        joinMessage.setSender(username);
        joinMessage.setContent(username + " dołączył do pokoju");
        joinMessage.setRoomId(roomId);
        joinMessage.setTimestamp(LocalDateTime.now());

        return joinMessage;
    }

    /**
     * Usuwanie wiadomości przez administratora / ownera (moderacja)
     */
    @MessageMapping("/chat.deleteMessage/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage deleteMessage(@DestinationVariable String roomId,
                                     @Payload ChatMessage chatMessage,
                                     SimpMessageHeaderAccessor headerAccessor) {

        String username = headerAccessor.getUser().getName();

        ChatMessage deleteMsg = new ChatMessage();
        deleteMsg.setType(ChatMessage.MessageType.CHAT);
        deleteMsg.setSender(username);
        deleteMsg.setContent("Wiadomość została usunięta przez administratora");
        deleteMsg.setRoomId(roomId);
        deleteMsg.setTimestamp(LocalDateTime.now());

        return deleteMsg;
    }
}
