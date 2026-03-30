package pl.workshop.chatapp.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(MessageService messageService,
                          FriendService friendService,
                          UserRepository userRepository,
                          SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.friendService = friendService;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable String roomId,
                                   @Payload ChatMessage chatMessage,
                                   SimpMessageHeaderAccessor headerAccessor) {
        return messageService.sendRoomMessage(roomId, chatMessage, headerAccessor);
    }

    @MessageMapping("/chat.sendPrivateMessage/{receiverUsername}")
    public void sendPrivateMessage(@DestinationVariable String receiverUsername,
                                   @Payload ChatMessage chatMessage,
                                   SimpMessageHeaderAccessor headerAccessor) {
        String senderEmail = extractAuthenticatedEmail(headerAccessor);

        User sender = userRepository.findByEmail(senderEmail).orElseThrow();
        User receiver = userRepository.findByUsername(receiverUsername.trim()).orElseThrow();

        if (!friendService.canSendPersonalMessage(sender, receiver)) {
            throw new IllegalStateException("Nie możesz wysłać wiadomości do tego użytkownika (brak przyjaźni lub ban)");
        }

        ChatMessage response = messageService.sendPrivateMessage(sender, receiver, chatMessage);
        messagingTemplate.convertAndSendToUser(receiver.getEmail(), "/queue/private", response);
        messagingTemplate.convertAndSendToUser(sender.getEmail(), "/queue/private", response);
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
        chatMessage.setSender(resolveBusinessUsername(sender));
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
        chatMessage.setSender(resolveBusinessUsername(sender));
        chatMessage.setTimestamp(LocalDateTime.now());

        return chatMessage;
    }

    private String extractAuthenticatedEmail(SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor == null || headerAccessor.getUser() == null || headerAccessor.getUser().getName() == null) {
            throw new IllegalStateException("Brak uwierzytelnionego użytkownika");
        }

        return headerAccessor.getUser().getName().trim().toLowerCase();
    }

    private String resolveBusinessUsername(User user) {
        if (user.getUsername() != null
                && !user.getUsername().isBlank()
                && !user.getUsername().equalsIgnoreCase(user.getEmail())) {
            return user.getUsername();
        }

        return userRepository.findById(user.getId())
                .map(User::getUsername)
                .orElse(user.getEmail());
    }
}
