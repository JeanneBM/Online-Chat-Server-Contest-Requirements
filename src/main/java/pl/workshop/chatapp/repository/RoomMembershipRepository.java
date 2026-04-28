package pl.workshop.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.RoomMembership;
import pl.workshop.chatapp.model.User;

import java.util.List;
import java.util.Optional;

public interface RoomMembershipRepository extends JpaRepository<RoomMembership, Long> {
    Optional<RoomMembership> findByRoomAndUser(Room room, User user);
    List<RoomMembership> findByUser(User user);
    List<RoomMembership> findByRoom(Room room);
}
