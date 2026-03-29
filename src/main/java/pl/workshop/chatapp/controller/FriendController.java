package pl.workshop.chatapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import pl.workshop.chatapp.model.*;
import pl.workshop.chatapp.repository.FriendRepository;
import pl.workshop.chatapp.repository.FriendRequestRepository;
import pl.workshop.chatapp.repository.UserRepository;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public FriendController(FriendRequestRepository friendRequestRepository,
                            FriendRepository friendRepository,
                            UserRepository userRepository,
                            SimpMessagingTemplate messagingTemplate) {
        this.friendRequestRepository = friendRequestRepository;
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // === REST ===
    @PostMapping("/request")
    public ResponseEntity<?> sendFriendRequest(@RequestBody Map<String, String> req, Principal principal) {
        String username = principal.getName();
        String targetUsername = req.get("username");

        User sender = userRepository.findByUsername(username).orElseThrow();
        User receiver = userRepository.findByUsername(targetUsername).orElseThrow();

        if (sender.equals(receiver)) return ResponseEntity.badRequest().body("Nie możesz dodać siebie");

        FriendRequest fr = new FriendRequest();
        fr.setSender(sender);
        fr.setReceiver(receiver);
        friendRequestRepository.save(fr);

        return ResponseEntity.ok("Zaproszenie wysłane");
    }

    @GetMapping("/requests")
    public List<FriendRequest> getMyRequests(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        return friendRequestRepository.findByReceiverAndStatus(user, FriendRequestStatus.PENDING);
    }

    @PostMapping("/accept/{requestId}")
    public ResponseEntity<?> acceptRequest(@PathVariable Long requestId, Principal principal) {
        FriendRequest fr = friendRequestRepository.findById(requestId).orElseThrow();
        if (!fr.getReceiver().getUsername().equals(principal.getName())) {
            return ResponseEntity.badRequest().body("Nie twoje zaproszenie");
        }
        fr.setStatus(FriendRequestStatus.ACCEPTED);
        friendRequestRepository.save(fr);

        // tworzymy relację friends
        Friend friend = new Friend();
        friend.setUser1(fr.getSender());
        friend.setUser2(fr.getReceiver());
        friendRepository.save(friend);

        return ResponseEntity.ok("Przyjęto");
    }

    @PostMapping("/ban")
    public ResponseEntity<?> banUser(@RequestBody Map<String, String> req, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        User target = userRepository.findByUsername(req.get("username")).orElseThrow();

        Friend friendship = friendRepository.findByUser1AndUser2(user, target)
                .orElseGet(() -> {
                    Friend f = new Friend();
                    f.setUser1(user);
                    f.setUser2(target);
                    return f;
                });
        friendship.setBannedByUser1(true);
        friendRepository.save(friendship);

        return ResponseEntity.ok("Użytkownik zbanowany");
    }

    // === WebSocket – personal message ===
    @MessageMapping("/chat.private")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage,
                                   SimpMessageHeaderAccessor headerAccessor) {

        String sender = headerAccessor.getUser().getName();
        String receiverUsername = chatMessage.getRoomId(); // w private używamy username jako "roomId"

        User receiver = userRepository.findByUsername(receiverUsername).orElseThrow();

        // sprawdź czy są friends i nie ma bana
        // (proste sprawdzenie – w pełni można rozbudować)

        messagingTemplate.convertAndSendToUser(receiverUsername, "/queue/private", chatMessage);
        messagingTemplate.convertAndSendToUser(sender, "/queue/private", chatMessage); // echo dla nadawcy
    }
}
