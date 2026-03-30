package pl.workshop.chatapp.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendTo;
import org.springframework.stereotype.Controller;
import pl.workshop.chatapp.model.ChatMessage;
import pl.workshop.chatapp.model.MessageType;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.UserRepository;
import pl.workshop.chatapp.service.FriendService;
import pl.workshop.chatapp.service.MessageService;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    private final MessageService messageService;
    private final FriendService friendService;
    private final UserRepository userRepository;

    public ChatController(MessageService messageService,
                          FriendService friendService,
                          UserRepository userRepository) {
        this.messageService = messageService;
        this.friendService = friendService;
        this.userRepository = userRepository;
    }

    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable String roomId,
                                   @Payload ChatMessage chatMessage,
                                   SimpMessageHeaderAccessor headerAccessor) {
        return messageService.sendRoomMessage(roomId, chatMessage, headerAccessor);
    }

    @MessageMapping("/chat.sendPrivateMessage/{receiverUsername}")
    @SendTo("/user/{receiverUsername}/queue/private")
    public ChatMessage sendPrivateMessage(@DestinationVariable String receiverUsername,
                                          @Payload ChatMessage chatMessage,
                                          SimpMessageHeaderAccessor headerAccessor) {
        String senderEmail = extractAuthenticatedEmail(headerAccessor);

        User sender = userRepository.findByEmail(senderEmail).orElseThrow();
        User receiver = userRepository.findByUsername(receiverUsername).orElseThrow();

        if (!friendService.canSendPersonalMessage(sender, receiver)) {
            throw new IllegalStateException("Nie możesz wysłać wiadomości do tego użytkownika (brak przyjaźni lub ban)");
        }

        return messageService.sendPrivateMessage(sender, receiver, chatMessage);
    }

    @MessageMapping("/chat.join/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage joinRoom(@DestinationVariable String roomId,
                                @Payload ChatMessage chatMessage,
                                SimpMessageHeaderAccessor headerAccessor) {
        String senderEmail = extractAuthenticatedEmail(headerAccessor);
        User sender = userRepository.findByEmail(senderEmail).orElseThrow();

        chatMessage.setType(MessageType.JOIN);
        chatMessage.setRoomId(roomId);
        chatMessage.setSender(sender.getUsername());
        chatMessage.setTimestamp(LocalDateTime.now());

        return chatMessage;
    }

    @MessageMapping("/chat.deleteMessage/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage deleteMessage(@DestinationVariable String roomId,
                                     @Payload ChatMessage chatMessage,
                                     SimpMessageHeaderAccessor headerAccessor) {
        String senderEmail = extractAuthenticatedEmail(headerAccessor);
        User sender = userRepository.findByEmail(senderEmail).orElseThrow();

        chatMessage.setType(MessageType.DELETE);
        chatMessage.setRoomId(roomId);
        chatMessage.setSender(sender.getUsername());
        chatMessage.setTimestamp(LocalDateTime.now());

        return chatMessage;
    }

    private String extractAuthenticatedEmail(SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor == null || headerAccessor.getUser() == null || headerAccessor.getUser().getName() == null) {
            throw new IllegalStateException("Brak uwierzytelnionego użytkownika");
        }

        return headerAccessor.getUser().getName().trim().toLowerCase();
    }
}
