package pl.workshop.chatapp.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import pl.workshop.chatapp.model.ChatMessage;
import pl.workshop.chatapp.model.Message;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.MessageRepository;
import pl.workshop.chatapp.repository.RoomRepository;
import pl.workshop.chatapp.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ChatController {

    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public ChatController(MessageRepository messageRepository, RoomRepository roomRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable String roomId, @Payload ChatMessage chatMessage,
                                   SimpMessageHeaderAccessor headerAccessor) {

        String username = headerAccessor.getUser().getName();
        User sender = userRepository.findByUsername(username).orElseThrow();

        Room room = roomRepository.findByName(roomId).orElseThrow();

        Message msg = new Message();
        msg.setRoom(room);
        msg.setSender(sender);
        msg.setContent(chatMessage.getContent());
        msg.setTimestamp(LocalDateTime.now());
        messageRepository.save(msg);

        chatMessage.setSender(username);
        chatMessage.setTimestamp(LocalDateTime.now());
        return chatMessage;
    }

    // Ładowanie historii przy wejściu do pokoju
    @MessageMapping("/chat.join/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage joinRoom(@DestinationVariable String roomId, SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        ChatMessage joinMsg = new ChatMessage();
        joinMsg.setType(ChatMessage.MessageType.JOIN);
        joinMsg.setSender(username);
        joinMsg.setContent(username + " dołączył do pokoju");
        return joinMsg;
    }
}
