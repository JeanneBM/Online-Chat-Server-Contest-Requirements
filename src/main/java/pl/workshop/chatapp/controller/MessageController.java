package pl.workshop.chatapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.workshop.chatapp.model.Message;
import pl.workshop.chatapp.service.MessageService;
import pl.workshop.chatapp.service.RoomService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final RoomService roomService;

    public MessageController(MessageService messageService, RoomService roomService) {
        this.messageService = messageService;
        this.roomService = roomService;
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Message>> getRoomMessages(@PathVariable Long roomId, Principal principal) {
        String email = extractAuthenticatedEmail(principal);
        return ResponseEntity.ok(messageService.getRoomMessages(roomId, email));
    }

    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> markRoomMessagesRead(@PathVariable Long roomId, Principal principal) {
        String email = extractAuthenticatedEmail(principal);
        roomService.markRoomMessagesRead(roomId, email);
        return ResponseEntity.ok("Wiadomości w pokoju oznaczone jako przeczytane");
    }

    @GetMapping("/private/{otherUsername}")
    public ResponseEntity<List<Message>> getPrivateMessages(@PathVariable String otherUsername, Principal principal) {
        String email = extractAuthenticatedEmail(principal);
        return ResponseEntity.ok(messageService.getPrivateMessages(email, otherUsername));
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
