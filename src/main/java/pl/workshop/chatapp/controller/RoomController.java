package pl.workshop.chatapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.RoomInvitation;
import pl.workshop.chatapp.model.RoomType;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.RoomRepository;
import pl.workshop.chatapp.repository.UserRepository;
import pl.workshop.chatapp.service.InvitationService;
import pl.workshop.chatapp.service.RoomService;

import java.security.Principal;
import java.util.List;
import java.util.Set;

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
        room.setName(roomReq.getName().trim());
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

    @PostMapping("/{roomId}/join")
    public ResponseEntity<?> joinPublicRoom(@PathVariable Long roomId, Principal principal) {
        return ResponseEntity.ok(roomService.joinPublicRoom(roomId, principal.getName()));
    }

    @PostMapping("/{roomId}/leave")
    public ResponseEntity<?> leaveRoom(@PathVariable Long roomId, Principal principal) {
        return ResponseEntity.ok(roomService.leaveRoom(roomId, principal.getName()));
    }

    @DeleteMapping("/{roomId}/message/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long roomId,
                                           @PathVariable Long messageId,
                                           Principal principal) {
        roomService.deleteMessage(roomId, messageId, principal.getName());
        return ResponseEntity.ok("Wiadomość usunięta");
    }

    @PostMapping("/{roomId}/ban")
    public ResponseEntity<?> banUserFromRoom(@PathVariable Long roomId,
                                             @RequestBody String username,
                                             Principal principal) {
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body("Username jest wymagany");
        }

        return ResponseEntity.ok(roomService.banUserFromRoom(roomId, username, principal.getName()));
    }

    @PostMapping("/{roomId}/remove")
    public ResponseEntity<?> removeMember(@PathVariable Long roomId,
                                          @RequestBody String username,
                                          Principal principal) {
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body("Username jest wymagany");
        }
        return ResponseEntity.ok(roomService.removeMember(roomId, username, principal.getName()));
    }

    @GetMapping("/{roomId}/bans")
    public ResponseEntity<Set<User>> getBannedUsers(@PathVariable Long roomId, Principal principal) {
        return ResponseEntity.ok(roomService.getBannedUsers(roomId, principal.getName()));
    }

    @DeleteMapping("/{roomId}/ban")
    public ResponseEntity<?> unbanUser(@PathVariable Long roomId,
                                       @RequestParam String username,
                                       Principal principal) {
        return ResponseEntity.ok(roomService.unbanUser(roomId, username, principal.getName()));
    }

    @PostMapping("/{roomId}/invite")
    public ResponseEntity<?> inviteToRoom(@PathVariable Long roomId,
                                          @RequestParam String username,
                                          Principal principal) {
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body("Username jest wymagany");
        }

        User inviter = userRepository.findByEmail(principal.getName()).orElseThrow();
        invitationService.inviteToRoom(roomId, inviter.getId(), username);

        return ResponseEntity.ok("Zaproszenie wysłane");
    }

    @GetMapping("/invitations")
    public ResponseEntity<List<RoomInvitation>> getPendingInvitations(Principal principal) {
        return ResponseEntity.ok(invitationService.getPendingInvitations(principal.getName()));
    }

    @PostMapping("/invitations/{invitationId}/accept")
    public ResponseEntity<?> acceptInvitation(@PathVariable Long invitationId, Principal principal) {
        invitationService.acceptInvitation(invitationId, principal.getName());
        return ResponseEntity.ok("Zaproszenie zaakceptowane");
    }

    @PostMapping("/invitations/{invitationId}/reject")
    public ResponseEntity<?> rejectInvitation(@PathVariable Long invitationId, Principal principal) {
        invitationService.rejectInvitation(invitationId, principal.getName());
        return ResponseEntity.ok("Zaproszenie odrzucone");
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long roomId, Principal principal) {
        roomService.deleteRoom(roomId, principal.getName());
        return ResponseEntity.ok("Pokój i wszystkie pliki usunięte");
    }
}
