package pl.workshop.chatapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.RoomType;
import pl.workshop.chatapp.repository.RoomRepository;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomRepository roomRepository;

    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Map<String, String> req, Principal principal) {
        // prosty create (później dodamy ownera)
        Room room = new Room();
        room.setName(req.get("name"));
        room.setDescription(req.get("description"));
        room.setType("public".equals(req.get("type")) ? RoomType.PUBLIC : RoomType.PRIVATE);
        // room.setOwner(...) - później
        Room saved = roomRepository.save(room);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/public")
    public List<Room> getPublicRooms() {
        return roomRepository.findByType(RoomType.PUBLIC);
    }
}
