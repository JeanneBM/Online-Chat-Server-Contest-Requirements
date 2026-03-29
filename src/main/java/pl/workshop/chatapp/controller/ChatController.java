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
import pl.workshop.chatapp.service.FriendService;
import pl.workshop.chatapp.service.MessageService;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    private final MessageService messageService;
    private final FriendService friendService;
    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public ChatController(MessageService messageService,
                          FriendService friendService,
                          MessageRepository messageRepository,
                          RoomRepository roomRepository,
                          UserRepository userRepository) {
        this.messageService = messageService;
        this.friendService = friendService;
        this.messageRepository = messageRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    // === WIADOMOŚCI W POKOJU ===
    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable String roomId,
                                   @Payload ChatMessage chatMessage,
                                   SimpMessageHeaderAccessor headerAccessor) {
        return messageService.sendRoomMessage(roomId, chatMessage, headerAccessor);
    }

    // === PERSONAL MESSAGING (DM) ===
    @MessageMapping("/chat.sendPrivateMessage/{receiverUsername}")
    @SendTo("/user/{receiverUsername}/queue/private")
    public ChatMessage sendPrivateMessage(@DestinationVariable String receiverUsername,
                                          @Payload ChatMessage chatMessage,
                                          SimpMessageHeaderAccessor headerAccessor) {

        String senderUsername = headerAccessor.getUser().getName();
        User sender = userRepository.findByUsername(senderUsername).orElseThrow();
        User receiver = userRepository.findByUsername(receiverUsername).orElseThrow();

        if (!friendService.canSendPersonalMessage(sender, receiver)) {
            throw new IllegalStateException("Nie możesz wysłać wiadomości do tego użytkownika (brak przyjaźni lub ban)");
        }

        return messageService.sendPrivateMessage(sender, receiver, chatMessage);
    }

    // reszta metod (join, deleteMessage) bez zmian...
    @MessageMapping("/chat.join/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage joinRoom(...) { /* bez zmian */ }

    @MessageMapping("/chat.deleteMessage/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage deleteMessage(...) { /* bez zmian */ }
}
