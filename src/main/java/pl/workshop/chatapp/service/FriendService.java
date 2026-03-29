package pl.workshop.chatapp.service;

import pl.workshop.chatapp.model.*;
import pl.workshop.chatapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendService {

    private final FriendRequestRepository friendRequestRepo;
    private final UserBanRepository userBanRepo;
    private final UserRepository userRepo;

    public void sendFriendRequest(Long senderId, String receiverUsername, String message) {
        User sender = userRepo.findById(senderId).orElseThrow();
        User receiver = userRepo.findByUsername(receiverUsername).orElseThrow();

        if (sender.equals(receiver)) throw new IllegalArgumentException("Nie możesz dodać samego siebie");

        FriendRequest existing = friendRequestRepo.findBySenderAndReceiver(sender, receiver).orElse(null);
        if (existing != null && existing.getStatus() == FriendRequest.FriendRequestStatus.PENDING) {
            throw new IllegalStateException("Zaproszenie już wysłane");
        }

        FriendRequest req = new FriendRequest();
        req.setSender(sender);
        req.setReceiver(receiver);
        req.setMessage(message);
        friendRequestRepo.save(req);
    }

    public void acceptFriendRequest(Long requestId, Long userId) {
        FriendRequest req = friendRequestRepo.findById(requestId).orElseThrow();
        if (!req.getReceiver().getId().equals(userId)) throw new SecurityException("Nie Twoja prośba");

        req.setStatus(FriendRequest.FriendRequestStatus.ACCEPTED);
        req.getSender().getFriends().add(req.getReceiver());
        req.getReceiver().getFriends().add(req.getSender());
        friendRequestRepo.save(req);
    }

    public void rejectFriendRequest(Long requestId, Long userId) { /* analogicznie */ }

    public void removeFriend(Long userId, Long friendId) {
        User user = userRepo.findById(userId).orElseThrow();
        User friend = userRepo.findById(friendId).orElseThrow();
        user.getFriends().remove(friend);
        friend.getFriends().remove(user);
    }

    public void banUser(Long bannerId, Long bannedId) {
        User banner = userRepo.findById(bannerId).orElseThrow();
        User banned = userRepo.findById(bannedId).orElseThrow();

        if (banner.getBans().stream().anyMatch(b -> b.getBanned().equals(banned))) return;

        UserBan ban = new UserBan();
        ban.setBanner(banner);
        ban.setBanned(banned);
        userBanRepo.save(ban);

        // zerwanie przyjaźni
        banner.getFriends().remove(banned);
        banned.getFriends().remove(banner);
    }

    public List<FriendRequest> getPendingRequests(Long userId) {
        User user = userRepo.findById(userId).orElseThrow();
        return friendRequestRepo.findByReceiverAndStatus(user, FriendRequest.FriendRequestStatus.PENDING);
    }

    public boolean canSendPersonalMessage(User sender, User receiver) {
        return sender.isFriend(receiver) &&
               !sender.hasBanned(receiver) &&
               !receiver.hasBanned(sender);
    }
}
