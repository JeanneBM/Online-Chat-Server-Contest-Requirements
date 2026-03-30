package pl.workshop.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.RoomInvitation;
import pl.workshop.chatapp.model.User;

import java.util.List;
import java.util.Optional;

public interface RoomInvitationRepository extends JpaRepository<RoomInvitation, Long> {
    List<RoomInvitation> findByInviteeAndAcceptedFalse(User invitee);
    Optional<RoomInvitation> findByIdAndInvitee(Long id, User invitee);
    boolean existsByRoomAndInviteeAndAcceptedFalse(Room room, User invitee);
    Optional<RoomInvitation> findByRoomAndInviteeAndAcceptedFalseAndRejectedFalse(Room room, User invitee);
    List<RoomInvitation> findByInviteeAndAcceptedFalseAndRejectedFalseOrderByCreatedAtDesc(User invitee);
}
