package pl.workshop.chatapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.RoomInvitation;
import pl.workshop.chatapp.model.RoomType;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.RoomInvitationRepository;
import pl.workshop.chatapp.repository.RoomRepository;
import pl.workshop.chatapp.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InvitationService {

    private final RoomInvitationRepository invitationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public RoomInvitation inviteToRoom(Long roomId, Long inviterId, String inviteeUsername) {
        User inviter = userRepository.findById(inviterId).orElseThrow();
        return inviteUserToPrivateRoom(roomId, inviter, inviteeUsername);
    }

    public RoomInvitation inviteUserToPrivateRoom(Long roomId, User inviter, String inviteeUsername) {
        if (inviter == null) {
            throw new IllegalArgumentException("Inviter jest wymagany");
        }

        if (inviteeUsername == null || inviteeUsername.isBlank()) {
            throw new IllegalArgumentException("Username zapraszanego użytkownika jest wymagany");
        }

        Room room = roomRepository.findById(roomId).orElseThrow();
        User invitee = userRepository.findByUsername(inviteeUsername.trim()).orElseThrow();

        if (room.getType() != RoomType.PRIVATE) {
            throw new IllegalStateException("Zaproszenia są dostępne tylko dla pokoi prywatnych");
        }

        if (!canInvite(room, inviter)) {
            throw new SecurityException("Nie masz uprawnień do zapraszania do tego pokoju");
        }

        if (room.getBannedUsers().contains(invitee)) {
            throw new IllegalStateException("Ten użytkownik jest zbanowany w tym pokoju");
        }

        if (isAlreadyInRoom(room, invitee)) {
            throw new IllegalStateException("Użytkownik już należy do pokoju");
        }

        boolean existsPending = invitationRepository
                .findByRoomAndInviteeAndAcceptedFalseAndRejectedFalse(room, invitee)
                .isPresent();

        if (existsPending) {
            throw new IllegalStateException("Oczekujące zaproszenie już istnieje");
        }

        RoomInvitation invitation = new RoomInvitation();
        invitation.setRoom(room);
        invitation.setInviter(inviter);
        invitation.setInvitee(invitee);
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setAccepted(false);
        invitation.setRejected(false);
        invitation.setRespondedAt(null);

        return invitationRepository.save(invitation);
    }

    @Transactional(readOnly = true)
    public List<RoomInvitation> getPendingInvitations(String email) {
        User invitee = findUserByEmail(email);
        return invitationRepository.findByInviteeAndAcceptedFalseAndRejectedFalseOrderByCreatedAtDesc(invitee);
    }

    public void acceptInvitation(Long invitationId, String email) {
        User invitee = findUserByEmail(email);
        RoomInvitation invitation = invitationRepository.findById(invitationId).orElseThrow();

        if (!invitation.getInvitee().equals(invitee)) {
            throw new SecurityException("To nie jest Twoje zaproszenie");
        }

        if (invitation.isAccepted()) {
            throw new IllegalStateException("Zaproszenie zostało już zaakceptowane");
        }

        if (invitation.isRejected()) {
            throw new IllegalStateException("Zaproszenie zostało już odrzucone");
        }

        Room room = invitation.getRoom();

        if (room.getBannedUsers().contains(invitee)) {
            throw new IllegalStateException("Nie możesz dołączyć do tego pokoju");
        }

        room.getMembers().add(invitee);
        roomRepository.save(room);

        invitation.setAccepted(true);
        invitation.setRejected(false);
        invitation.setRespondedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
    }

    public void rejectInvitation(Long invitationId, String email) {
        User invitee = findUserByEmail(email);
        RoomInvitation invitation = invitationRepository.findById(invitationId).orElseThrow();

        if (!invitation.getInvitee().equals(invitee)) {
            throw new SecurityException("To nie jest Twoje zaproszenie");
        }

        if (invitation.isAccepted()) {
            throw new IllegalStateException("Zaproszenie zostało już zaakceptowane");
        }

        if (invitation.isRejected()) {
            throw new IllegalStateException("Zaproszenie zostało już odrzucone");
        }

        invitation.setRejected(true);
        invitation.setAccepted(false);
        invitation.setRespondedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
    }

    private User findUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email jest wymagany");
        }
        return userRepository.findByEmail(email.trim().toLowerCase()).orElseThrow();
    }

    private boolean canInvite(Room room, User user) {
        return room.getOwner().equals(user)
                || room.getAdmins().contains(user)
                || room.getMembers().contains(user);
    }

    private boolean isAlreadyInRoom(Room room, User user) {
        return room.getOwner().equals(user)
                || room.getAdmins().contains(user)
                || room.getMembers().contains(user);
    }
}
