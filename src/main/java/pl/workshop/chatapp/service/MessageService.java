package pl.workshop.chatapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.workshop.chatapp.model.Message;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.MessageRepository;
import pl.workshop.chatapp.repository.RoomRepository;
import pl.workshop.chatapp.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public Message sendRoomMessage(Long roomId, String email, String content, String attachmentUrl, Long replyToId) {
        User sender = findUserByEmail(email);
        Room room = roomRepository.findById(roomId).orElseThrow();

        if (!isRoomMember(room, sender)) {
            throw new SecurityException("Nie należysz do tego pokoju");
        }

        if (room.getBannedUsers().contains(sender)) {
            throw new SecurityException("Jesteś zbanowany w tym pokoju");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setRoom(room);
        message.setContent(content);
        message.setAttachmentUrl(attachmentUrl);
        message.setTimestamp(LocalDateTime.now());

        if (replyToId != null) {
            Message replyTo = messageRepository.findById(replyToId).orElseThrow();
            if (replyTo.getRoom() == null || !replyTo.getRoom().getId().equals(roomId)) {
                throw new IllegalArgumentException("Nieprawidłowa wiadomość do odpowiedzi");
            }
            message.setReplyTo(replyTo);
        }

        return messageRepository.save(message);
    }

    public Message sendPrivateMessage(String senderEmail, String receiverUsername, String content, String attachmentUrl, Long replyToId) {
        User sender = findUserByEmail(senderEmail);
        User receiver = findUserByUsername(receiverUsername);

        if (!areFriends(sender, receiver)) {
            throw new SecurityException("Wiadomości prywatne są dostępne tylko dla znajomych");
        }

        if (isBlockedEitherWay(sender, receiver)) {
            throw new SecurityException("Nie można wysłać wiadomości do tego użytkownika");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setAttachmentUrl(attachmentUrl);
        message.setTimestamp(LocalDateTime.now());

        if (replyToId != null) {
            Message replyTo = messageRepository.findById(replyToId).orElseThrow();

            boolean belongsToConversation =
                    (replyTo.getSender().equals(sender) && replyTo.getReceiver() != null && replyTo.getReceiver().equals(receiver)) ||
                    (replyTo.getSender().equals(receiver) && replyTo.getReceiver() != null && replyTo.getReceiver().equals(sender));

            if (!belongsToConversation) {
                throw new IllegalArgumentException("Nieprawidłowa wiadomość do odpowiedzi");
            }

            message.setReplyTo(replyTo);
        }

        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<Message> getRoomMessages(Long roomId, String email) {
        User user = findUserByEmail(email);
        Room room = roomRepository.findById(roomId).orElseThrow();

        if (!isRoomMember(room, user)) {
            throw new SecurityException("Brak dostępu do pokoju");
        }

        return messageRepository.findByRoomOrderByTimestampAsc(room);
    }

    @Transactional(readOnly = true)
    public List<Message> getPrivateMessages(String email, String otherUsername) {
        User user = findUserByEmail(email);
        User other = findUserByUsername(otherUsername);

        if (!areFriends(user, other) && !hasAnyPrivateMessages(user, other)) {
            throw new SecurityException("Brak dostępu do tej rozmowy");
        }

        return messageRepository.findConversation(user, other);
    }

    public void markPrivateMessagesRead(String email, String otherUsername) {
        User user = findUserByEmail(email);
        User other = findUserByUsername(otherUsername);

        List<Message> conversation = messageRepository.findConversation(user, other);

        conversation.stream()
                .filter(message -> other.equals(message.getSender()))
                .filter(message -> user.equals(message.getReceiver()))
                .filter(message -> message.getReadAt() == null)
                .forEach(message -> message.setReadAt(LocalDateTime.now()));
    }

    @Transactional(readOnly = true)
    public long getUnreadPrivateCount(String email) {
        User user = findUserByEmail(email);
        return messageRepository.countByReceiverAndReadAtIsNull(user);
    }

    @Transactional(readOnly = true)
    public long getUnreadRoomCount(Long roomId, String email) {
        User user = findUserByEmail(email);
        Room room = roomRepository.findById(roomId).orElseThrow();

        if (!isRoomMember(room, user)) {
            throw new SecurityException("Brak dostępu do pokoju");
        }

        return messageRepository.findByRoomOrderByTimestampAsc(room).stream()
                .filter(message -> !user.equals(message.getSender()))
                .filter(message -> message.getReadAt() == null)
                .count();
    }

    private User findUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email jest wymagany");
        }
        return userRepository.findByEmail(email.trim().toLowerCase()).orElseThrow();
    }

    private User findUserByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username jest wymagany");
        }
        return userRepository.findByUsername(username.trim()).orElseThrow();
    }

    private boolean isRoomMember(Room room, User user) {
        return room.getOwner().equals(user)
                || room.getAdmins().contains(user)
                || room.getMembers().contains(user);
    }

    private boolean areFriends(User a, User b) {
        return a.getFriends() != null && a.getFriends().contains(b)
                && b.getFriends() != null && b.getFriends().contains(a);
    }

    private boolean isBlockedEitherWay(User a, User b) {
        return (a.getBannedUsers() != null && a.getBannedUsers().contains(b))
                || (b.getBannedUsers() != null && b.getBannedUsers().contains(a));
    }

    private boolean hasAnyPrivateMessages(User a, User b) {
        return !messageRepository.findConversation(a, b).isEmpty();
    }
}
