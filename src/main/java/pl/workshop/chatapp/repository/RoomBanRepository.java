package pl.workshop.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.RoomBan;
import pl.workshop.chatapp.model.User;

import java.util.List;
import java.util.Optional;

public interface RoomBanRepository extends JpaRepository<RoomBan, Long> {
    Optional<RoomBan> findByRoomAndBannedUser(Room room, User bannedUser);
    List<RoomBan> findByRoomOrderByBannedAtDesc(Room room);
    void deleteByRoomAndBannedUser(Room room, User bannedUser);
}
