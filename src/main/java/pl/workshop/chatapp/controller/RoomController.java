package pl.workshop.chatapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.RoomType;
import pl.workshop.chatapp.repository.RoomRepository;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomRepository roomRepository;

    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room, Principal principal) {
        room.setOwner(/* później powiążemy z aktualnym userem */);
        Room saved = roomRepository.save(room);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/public")
    public List<Room> getPublicRooms() {
        return roomRepository.findByType(RoomType.PUBLIC);
    }

    @GetMapping("/{roomId}/history")
    public List<Message> getHistory(@PathVariable Long roomId) {  // później dodamy
        // tymczasowo puste – uzupełnimy w następnym kroku
        return List.of();
    }
}
