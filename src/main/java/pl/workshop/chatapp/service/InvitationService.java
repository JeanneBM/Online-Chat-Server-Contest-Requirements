package pl.workshop.chatapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.RoomInvitation;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.RoomInvitationRepository;
import pl.workshop.chatapp.repository.RoomRepository;
import pl.workshop.chatapp.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final RoomInvitationRepository invitationRepo;
    private final RoomRepository roomRepo;
    private final UserRepository userRepo;

    public void inviteToRoom(Long roomId, Long inviterId, String inviteeUsername) {
        Room room = roomRepo.findById(roomId).orElseThrow();
        if (!room.getOwner().getId().equals(inviterId) && !room.getAdmins().contains(userRepo.findById(inviterId).orElseThrow())) {
            throw new SecurityException("Tylko owner/admin może zapraszać");
        }
        User invitee = userRepo.findByUsername(inviteeUsername).orElseThrow();

        RoomInvitation inv = new RoomInvitation();
        inv.setRoom(room);
        inv.setInviter(userRepo.findById(inviterId).orElseThrow());
        inv.setInvitee(invitee);
        invitationRepo.save(inv);
    }
}
