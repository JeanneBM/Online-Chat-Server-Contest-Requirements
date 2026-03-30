package pl.workshop.chatapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.RoomType;
import pl.workshop.chatapp.repository.RoomRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public List<Room> getPublicRooms(String search) {
        List<Room> publicRooms = roomRepository.findByType(RoomType.PUBLIC);
        if (search == null || search.isBlank()) {
            return publicRooms;
        }

        String needle = search.toLowerCase();
        return publicRooms.stream()
                .filter(room -> (room.getName() != null && room.getName().toLowerCase().contains(needle))
                        || (room.getDescription() != null && room.getDescription().toLowerCase().contains(needle)))
                .toList();
    }
}
