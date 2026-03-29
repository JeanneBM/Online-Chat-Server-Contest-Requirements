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
    public ResponseEntity<?> createRoom(@RequestBody Room roomReq, Principal principal) {
        User owner = userRepository.findByUsername(principal.getName()).orElseThrow();

        if (roomReq.getName() == null || roomReq.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Nazwa pokoju jest wymagana");
        }

        if (roomRepository.existsByName(roomReq.getName())) {
            return ResponseEntity.badRequest().body("Pokój o tej nazwie już istnieje");
        }

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

    @GetMapping("/public")
    public ResponseEntity<List<Room>> getPublicRooms(@RequestParam(required = false) String search) {
        List<Room> rooms;

        if (search == null || search.isBlank()) {
            rooms = roomRepository.findByType(RoomType.PUBLIC);
        } else {
            rooms = roomService.getPublicRooms(search);
        }

        return ResponseEntity.ok(rooms);
    }

    @DeleteMapping("/{roomId}/message/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long roomId,
                                           @PathVariable Long messageId,
                                           Principal principal) {
        return ResponseEntity.ok("Message deletion endpoint prepared");
    }

    @PostMapping("/{roomId}/ban")
    public ResponseEntity<?> banUserFromRoom(@PathVariable Long roomId,
                                             @RequestBody String username,
                                             Principal principal) {
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body("Username jest wymagany");
        }

        return ResponseEntity.ok("User ban endpoint prepared");
    }

    @PostMapping("/{roomId}/invite")
    public ResponseEntity<?> inviteToRoom(@PathVariable Long roomId,
                                          @RequestParam String username,
                                          Principal principal) {
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body("Username jest wymagany");
        }

        User inviter = userRepository.findByUsername(principal.getName()).orElseThrow();
        invitationService.inviteToRoom(roomId, inviter.getId(), username);

        return ResponseEntity.ok("Zaproszenie wysłane");
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long roomId, Principal principal) {
        Room room = roomRepository.findById(roomId).orElseThrow();

        if (!room.getOwner().getUsername().equals(principal.getName())) {
            return ResponseEntity.badRequest().body("Tylko owner może usunąć pokój");
        }

        roomRepository.delete(room);
        return ResponseEntity.ok("Pokój i wszystkie pliki usunięte");
    }
}
