package pl.workshop.chatapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.RoomType;
import pl.workshop.chatapp.model.User;
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
        User owner = userRepository.findByUsername(principal.getName()).orElseThrow();

        Room room = new Room();
        room.setName(roomReq.getName());
        room.setDescription(roomReq.getDescription());
        room.setType(roomReq.getType() != null ? roomReq.getType() : RoomType.PUBLIC);
        room.setOwner(owner);
        room.getMembers().add(owner);
        room.getAdmins().add(owner);

        Room saved = roomRepository.save(room);
        return ResponseEntity.ok(saved);
    }

    // === KATALOG PUBLICZNYCH POKOI + SEARCH ===
    @GetMapping("/public")
    public List<Room> getPublicRooms(@RequestParam(required = false) String search) {
        if (search == null || search.isBlank()) {
            return roomRepository.findByType(RoomType.PUBLIC);
        }
        return roomService.getPublicRooms(search);
    }

    // === MODERACJA ===
    @DeleteMapping("/{roomId}/message/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long roomId,
                                           @PathVariable Long messageId,
                                           Principal principal) {
        // tutaj można później dodać właściwą logikę sprawdzania admina/owner'a
        return ResponseEntity.ok("Message deleted (backend ready)");
    }

    @PostMapping("/{roomId}/ban")
    public ResponseEntity<?> banUserFromRoom(@PathVariable Long roomId,
                                             @RequestBody String username,
                                             Principal principal) {
        // tutaj można później dodać właściwą logikę banowania
        return ResponseEntity.ok("User banned from room");
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
        Room room = roomRepository.findById(roomId).orElseThrow();

        if (!room.getOwner().getUsername().equals(principal.getName())) {
            return ResponseEntity.badRequest().body("Tylko owner może usunąć pokój");
        }

        roomRepository.delete(room);
        return ResponseEntity.ok("Pokój i wszystkie pliki usunięte");
    }

    // Delete account (2.1.5)
    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();

        roomRepository.findAll().stream()
                .filter(r -> r.getOwner().equals(user))
                .forEach(roomRepository::delete);

        userRepository.delete(user);
        return ResponseEntity.ok("Konto usunięte");
    }
}
