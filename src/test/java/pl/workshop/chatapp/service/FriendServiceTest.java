package pl.workshop.chatapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.workshop.chatapp.model.FriendRequest;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.FriendRequestRepository;
import pl.workshop.chatapp.repository.UserBanRepository;
import pl.workshop.chatapp.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock
    private FriendRequestRepository friendRequestRepo;

    @Mock
    private UserBanRepository userBanRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private FriendService friendService;

    private User sender;
    private User receiver;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");
    }

    @Test
    void sendFriendRequestShouldSaveWhenNoBlockers() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepo.findByUsername("receiver")).thenReturn(Optional.of(receiver));
        when(userBanRepo.existsByBannerAndBanned(sender, receiver)).thenReturn(false);
        when(userBanRepo.existsByBannerAndBanned(receiver, sender)).thenReturn(false);
        when(friendRequestRepo.findBySenderAndReceiver(sender, receiver)).thenReturn(Optional.empty());

        friendService.sendFriendRequest(1L, "receiver", "Cześć!");

        verify(friendRequestRepo).save(any(FriendRequest.class));
    }

    @Test
    void sendFriendRequestShouldThrowWhenUsersAreBanned() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepo.findByUsername("receiver")).thenReturn(Optional.of(receiver));
        when(userBanRepo.existsByBannerAndBanned(sender, receiver)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> friendService.sendFriendRequest(1L, "receiver", "Cześć!"));

        verify(friendRequestRepo, never()).save(any(FriendRequest.class));
    }

    @Test
    void acceptFriendRequestShouldThrowForNonReceiver() {
        FriendRequest request = new FriendRequest();
        request.setId(10L);
        request.setSender(sender);
        request.setReceiver(receiver);

        when(friendRequestRepo.findById(10L)).thenReturn(Optional.of(request));

        assertThrows(SecurityException.class, () -> friendService.acceptFriendRequest(10L, 999L));

        verify(friendRequestRepo, never()).save(any(FriendRequest.class));
    }

    @Test
    void canSendPersonalMessageShouldRespectFriendshipAndBanChecks() {
        sender.getFriends().add(receiver);

        when(userBanRepo.existsByBannerAndBanned(sender, receiver)).thenReturn(false);
        when(userBanRepo.existsByBannerAndBanned(receiver, sender)).thenReturn(false);

        assertTrue(friendService.canSendPersonalMessage(sender, receiver));

        when(userBanRepo.existsByBannerAndBanned(receiver, sender)).thenReturn(true);

        assertFalse(friendService.canSendPersonalMessage(sender, receiver));
    }
}
