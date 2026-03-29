package pl.workshop.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.workshop.chatapp.model.RoomInvitation;
import pl.workshop.chatapp.model.User;

import java.util.List;

public interface RoomInvitationRepository extends JpaRepository<RoomInvitation, Long> {
    List<RoomInvitation> findByInviteeAndAcceptedFalse(User invitee);
}
