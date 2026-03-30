package pl.workshop.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.workshop.chatapp.model.Message;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.User;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRoomOrderByTimestampAsc(Room room);
    List<Message> findBySenderAndReceiverOrderByTimestampAsc(User sender, User receiver);
    List<Message> findByReceiverAndSenderOrderByTimestampAsc(User receiver, User sender);
    Optional<Message> findByIdAndRoom(Long id, Room room);

    @Query("""
            select count(m) from Message m
            where m.receiver = :user
              and m.readAt is null
            """)
    long countUnreadPrivateMessages(@Param("user") User user);

    @Query("""
            select count(m) from Message m
            where m.room in :rooms
              and m.sender <> :user
              and m.readAt is null
            """)
    long countUnreadRoomMessages(@Param("user") User user, @Param("rooms") List<Room> rooms);
}
