package pl.workshop.chatapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import pl.workshop.chatapp.model.ChatMessage;
import pl.workshop.chatapp.model.Message;
import pl.workshop.chatapp.service.MessageService;
import pl.workshop.chatapp.service.RoomService;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.UserRepository;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public MessageController(MessageService messageService,
                             RoomService roomService,
                             SimpMessagingTemplate messagingTemplate, UserRepository userRepository) {
        this.messageService = messageService;
        this.roomService = roomService;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ChatMessage>> getRoomMessages(@PathVariable Long roomId, Principal principal) {
        String email = extractAuthenticatedEmail(principal);
        return ResponseEntity.ok(messageService.getRoomMessages(roomId, email).stream().map(this::toDto).toList());
    }

    @PostMapping("/room/{roomKey}")
    public ResponseEntity<ChatMessage> sendRoomMessage(@PathVariable String roomKey,
                                                       @RequestBody ChatMessage chatMessage,
                                                       Principal principal) {
        String email = extractAuthenticatedEmail(principal);
        ChatMessage response = messageService.sendRoomMessage(roomKey, chatMessage, email);
        messagingTemplate.convertAndSend("/topic/room/" + response.getRoomId(), response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> markRoomMessagesRead(@PathVariable Long roomId, Principal principal) {
        String email = extractAuthenticatedEmail(principal);
        roomService.markRoomMessagesRead(roomId, email);
        return ResponseEntity.ok("Wiadomości w pokoju oznaczone jako przeczytane");
    }

    @GetMapping("/private/{otherUsername}")
    public ResponseEntity<List<ChatMessage>> getPrivateMessages(@PathVariable String otherUsername, Principal principal) {
        String email = extractAuthenticatedEmail(principal);
        return ResponseEntity.ok(messageService.getPrivateMessages(email, otherUsername).stream().map(this::toDto).toList());
    }

    @PostMapping("/private/{otherUsername}")
    public ResponseEntity<ChatMessage> sendPrivateMessage(@PathVariable String otherUsername,
                                                         @RequestBody ChatMessage message, Principal principal) {
        User sender = userRepository.findByEmail(extractAuthenticatedEmail(principal)).orElseThrow();
        User receiver = userRepository.findByUsername(otherUsername).orElseThrow();
        ChatMessage response = messageService.sendPrivateMessage(sender, receiver, message);
        messagingTemplate.convertAndSendToUser(receiver.getEmail(), "/queue/private", response);
        messagingTemplate.convertAndSendToUser(sender.getEmail(), "/queue/private", response);
        return ResponseEntity.ok(response);
    }

    private ChatMessage toDto(Message message) {
        ChatMessage dto = new ChatMessage();
        dto.setId(message.getId());
        dto.setSender(message.getSender().getUsername());
        dto.setReceiver(message.getReceiver() == null ? null : message.getReceiver().getUsername());
        dto.setRoomId(message.getRoom() == null ? null : message.getRoom().getId().toString());
        dto.setContent(message.getContent());
        dto.setAttachmentUrl(message.getAttachmentUrl());
        dto.setTimestamp(message.getTimestamp());
        dto.setReplyToId(message.getReplyToId());
        return dto;
    }

    @PostMapping("/private/{otherUsername}/read")
    public ResponseEntity<?> markPrivateMessagesRead(@PathVariable String otherUsername, Principal principal) {
        String email = extractAuthenticatedEmail(principal);
        messageService.markPrivateMessagesRead(email, otherUsername);
        return ResponseEntity.ok("Wiadomości prywatne oznaczone jako przeczytane");
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCounts(Principal principal) {
        String email = extractAuthenticatedEmail(principal);

        return ResponseEntity.ok(Map.of(
                "privateMessages", messageService.getUnreadPrivateCount(email),
                "roomMessages", messageService.getUnreadRoomCount(email)
        ));
    }

    private String extractAuthenticatedEmail(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new IllegalStateException("Brak uwierzytelnionego użytkownika");
        }

        return principal.getName().trim().toLowerCase();
    }
}
