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
        return ResponseEntity.ok(messageService.getRoomMessages(roomId, principal.getName()));
    }

    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> markRoomMessagesRead(@PathVariable Long roomId, Principal principal) {
        roomService.markRoomMessagesRead(roomId, principal.getName());
        return ResponseEntity.ok("Wiadomości w pokoju oznaczone jako przeczytane");
    }

    @GetMapping("/private/{otherUsername}")
    public ResponseEntity<List<Message>> getPrivateMessages(@PathVariable String otherUsername, Principal principal) {
        return ResponseEntity.ok(messageService.getPrivateMessages(principal.getName(), otherUsername));
    }

    @PostMapping("/private/{otherUsername}/read")
    public ResponseEntity<?> markPrivateMessagesRead(@PathVariable String otherUsername, Principal principal) {
        messageService.markPrivateMessagesRead(principal.getName(), otherUsername);
        return ResponseEntity.ok("Wiadomości prywatne oznaczone jako przeczytane");
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCounts(Principal principal) {
        return ResponseEntity.ok(Map.of(
                "privateMessages", messageService.getUnreadPrivateCount(principal.getName()),
                "roomMessages", messageService.getUnreadRoomCount(principal.getName())
        ));
    }
}
