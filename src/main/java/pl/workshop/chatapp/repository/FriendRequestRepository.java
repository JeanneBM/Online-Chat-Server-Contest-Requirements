package pl.workshop.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.workshop.chatapp.model.FriendRequest;
import pl.workshop.chatapp.model.User;
import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findByReceiverAndStatus(User receiver, FriendRequestStatus status);
    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);
}
