package pl.workshop.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.workshop.chatapp.model.RoomMembership;

public interface RoomMembershipRepository extends JpaRepository<RoomMembership, Long> {
}
