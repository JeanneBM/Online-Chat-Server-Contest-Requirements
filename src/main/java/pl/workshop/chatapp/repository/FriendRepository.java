package pl.workshop.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.workshop.chatapp.model.Friend;
import pl.workshop.chatapp.model.User;
import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findByUser1(User user1);
    Optional<Friend> findByUser1AndUser2(User user1, User user2);
}
