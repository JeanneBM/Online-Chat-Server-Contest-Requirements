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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InvitationService {

    private final RoomInvitationRepository invitationRepo;
    private final RoomRepository roomRepo;
    private final UserRepository userRepo;

    public void inviteToRoom(Long roomId, Long inviterId, String inviteeUsername) {
        Room room = roomRepo.findById(roomId).orElseThrow();
        User inviter = userRepo.findById(inviterId).orElseThrow();

        if (!room.getOwner().getId().equals(inviterId) && !room.getAdmins().contains(inviter)) {
            throw new SecurityException("Tylko owner/admin może zapraszać");
        }
        if (room.getType() != RoomType.PRIVATE) {
            throw new IllegalStateException("Zaproszenia są wymagane tylko dla pokoi prywatnych");
        }

        User invitee = userRepo.findByUsername(inviteeUsername.trim()).orElseThrow();
        if (room.getBannedUsers().contains(invitee)) {
            throw new IllegalStateException("Użytkownik jest zbanowany w tym pokoju");
        }
        if (room.getMembers().contains(invitee)) {
            throw new IllegalStateException("Użytkownik już jest członkiem pokoju");
        }
        if (invitationRepo.existsByRoomAndInviteeAndAcceptedFalse(room, invitee)) {
            throw new IllegalStateException("Aktywne zaproszenie już istnieje");
        }

        RoomInvitation inv = new RoomInvitation();
        inv.setRoom(room);
        inv.setInviter(inviter);
        inv.setInvitee(invitee);
        invitationRepo.save(inv);
    }

    public List<RoomInvitation> getPendingInvitations(String username) {
        User invitee = userRepo.findByUsername(username).orElseThrow();
        return invitationRepo.findByInviteeAndAcceptedFalse(invitee);
    }

    public void acceptInvitation(Long invitationId, String username) {
        User invitee = userRepo.findByUsername(username).orElseThrow();
        RoomInvitation invitation = invitationRepo.findByIdAndInvitee(invitationId, invitee).orElseThrow();

        if (invitation.getRoom().getBannedUsers().contains(invitee)) {
            throw new IllegalStateException("Użytkownik jest zbanowany w tym pokoju");
        }

        invitation.setAccepted(true);
        invitation.getRoom().getMembers().add(invitee);
        invitationRepo.save(invitation);
        roomRepo.save(invitation.getRoom());
    }

    public void rejectInvitation(Long invitationId, String username) {
        User invitee = userRepo.findByUsername(username).orElseThrow();
        RoomInvitation invitation = invitationRepo.findByIdAndInvitee(invitationId, invitee).orElseThrow();
        invitationRepo.delete(invitation);
    }
}
