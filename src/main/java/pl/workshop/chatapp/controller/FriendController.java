package pl.workshop.chatapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import pl.workshop.chatapp.controller.dto.FriendDto;
import pl.workshop.chatapp.model.FriendRequest;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.FriendRequestRepository;
import pl.workshop.chatapp.repository.UserRepository;
import pl.workshop.chatapp.service.FriendService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<?> sendRequest(@RequestParam(required = false) String username,
                                         @RequestParam(required = false) String message,
                                         @RequestBody(required = false) Map<String, String> body,
                                         Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        String resolvedUsername = username;
        String resolvedMessage = message;
        if ((resolvedUsername == null || resolvedUsername.isBlank()) && body != null) {
            resolvedUsername = body.get("username");
        }
        if ((resolvedMessage == null || resolvedMessage.isBlank()) && body != null) {
            resolvedMessage = body.get("message");
        }
        friendService.sendFriendRequest(user.getId(), resolvedUsername, resolvedMessage);
        return ResponseEntity.ok("Zaproszenie wysłane");
    }

    @GetMapping
    public ResponseEntity<List<FriendDto>> getFriends(Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        List<FriendDto> friends = friendService.getFriends(user.getId())
                .stream()
                .map(FriendDto::from)
                .toList();
        return ResponseEntity.ok(friends);
    }

    @PostMapping("/request/{requestId}/accept")
    public ResponseEntity<?> acceptRequest(@PathVariable Long requestId, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        friendService.acceptFriendRequest(requestId, user.getId());
        return ResponseEntity.ok("Przyjaźń zaakceptowana");
    }

    @PostMapping("/request/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long requestId, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        friendService.rejectFriendRequest(requestId, user.getId());
        return ResponseEntity.ok("Zaproszenie odrzucone");
    }

    @GetMapping("/requests/pending")
    public List<FriendRequest> getPendingRequests(Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        return friendService.getPendingRequests(user.getId());
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<?> removeFriend(@PathVariable Long friendId, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        friendService.removeFriend(user.getId(), friendId);
        return ResponseEntity.ok("Przyjaciel usunięty");
    }

    @PostMapping("/ban/{userId}")
    public ResponseEntity<?> banUser(@PathVariable Long userId, Principal principal) {
        User banner = userRepository.findByEmail(principal.getName()).orElseThrow();
        friendService.banUser(banner.getId(), userId);
        return ResponseEntity.ok("Użytkownik zbanowany");
    }
}
