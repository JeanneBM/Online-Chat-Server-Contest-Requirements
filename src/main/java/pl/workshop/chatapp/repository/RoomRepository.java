package pl.workshop.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.RoomType;
import pl.workshop.chatapp.model.User;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByName(String name);
    List<Room> findByType(RoomType type);
    boolean existsByName(String name);
    List<Room> findByOwner(User owner);

    @Query("""
            select rm.room from RoomMembership rm
            where rm.user = :user
            """)
    List<Room> findActiveRoomsByUser(@Param("user") User user);
}
