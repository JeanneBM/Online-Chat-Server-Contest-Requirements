package pl.workshop.chatapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.RoomType;
import pl.workshop.chatapp.repository.RoomRepository;
import pl.workshop.chatapp.repository.UserRepository;
import pl.workshop.chatapp.service.InvitationService;
import pl.workshop.chatapp.service.RoomService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomService roomService;
    private final InvitationService invitationService;

    public RoomController(RoomRepository roomRepository,
                          UserRepository userRepository,
                          RoomService roomService,
                          InvitationService invitationService) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.roomService = roomService;
        this.invitationService = invitationService;
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room roomReq, Principal principal) {
        // logika tworzenia pokoju (z Twojego istniejącego kodu)
        return ResponseEntity.ok(roomService.createRoom(roomReq, principal));
    }

    // === KATALOG PUBLICZNYCH POKOI + SEARCH ===
    @GetMapping("/public")
    public List<Room> getPublicRooms(@RequestParam(required = false) String search) {
        return roomService.getPublicRooms(search);
    }

    // === ZAPROSZENIA DO PRYWATNYCH POKOI ===
    @PostMapping("/{roomId}/invite")
    public ResponseEntity<?> inviteToRoom(@PathVariable Long roomId,
                                          @RequestParam String username,
                                          Principal principal) {
        Long inviterId = Long.valueOf(principal.getName());
        invitationService.inviteToRoom(roomId, inviterId, username);
        return ResponseEntity.ok("Zaproszenie wysłane");
    }

    // === MODERACJA I USUWANIE ===
    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long roomId, Principal principal) {
        return roomService.deleteRoom(roomId, principal);
    }

    // (pozostałe metody ban, delete message itd. możesz zostawić lub dodać z poprzednich wersji)
}
