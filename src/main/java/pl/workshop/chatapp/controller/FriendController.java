package pl.workshop.chatapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import pl.workshop.chatapp.model.*;
import pl.workshop.chatapp.repository.FriendRepository;
import pl.workshop.chatapp.repository.FriendRequestRepository;
import pl.workshop.chatapp.repository.UserBanRepository;
import pl.workshop.chatapp.repository.UserRepository;
import pl.workshop.chatapp.service.FriendService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public FriendController(FriendService friendService,
                            FriendRequestRepository friendRequestRepository,
                            UserRepository userRepository,
                            SimpMessagingTemplate messagingTemplate) {
        this.friendService = friendService;
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/request")
    public ResponseEntity<?> sendRequest(@RequestParam String username,
                                         @RequestParam(required = false) String message,
                                         Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        friendService.sendFriendRequest(userId, username, message);
        return ResponseEntity.ok("Zaproszenie wysłane");
    }

    @PostMapping("/request/{requestId}/accept")
    public ResponseEntity<?> acceptRequest(@PathVariable Long requestId, Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        friendService.acceptFriendRequest(requestId, userId);
        return ResponseEntity.ok("Przyjaźń zaakceptowana");
    }

    @PostMapping("/request/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long requestId, Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        friendService.rejectFriendRequest(requestId, userId);
        return ResponseEntity.ok("Zaproszenie odrzucone");
    }

    @GetMapping("/requests/pending")
    public List<FriendRequest> getPendingRequests(Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        return friendService.getPendingRequests(userId);
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<?> removeFriend(@PathVariable Long friendId, Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        friendService.removeFriend(userId, friendId);
        return ResponseEntity.ok("Przyjaciel usunięty");
    }

    @PostMapping("/ban/{userId}")
    public ResponseEntity<?> banUser(@PathVariable Long userId, Principal principal) {
        Long bannerId = Long.valueOf(principal.getName());
        friendService.banUser(bannerId, userId);
        return ResponseEntity.ok("Użytkownik zbanowany");
    }
}
