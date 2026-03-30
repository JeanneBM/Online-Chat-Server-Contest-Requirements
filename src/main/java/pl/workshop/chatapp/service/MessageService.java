package pl.workshop.chatapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.workshop.chatapp.model.ChatMessage;
import pl.workshop.chatapp.model.Message;
import pl.workshop.chatapp.model.MessageType;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.MessageRepository;
import pl.workshop.chatapp.repository.RoomRepository;
import pl.workshop.chatapp.repository.UserBanRepository;
import pl.workshop.chatapp.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final UserBanRepository userBanRepository;

    public ChatMessage sendRoomMessage(String roomId, ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String senderEmail = extractAuthenticatedEmail(headerAccessor);
        Long parsedRoomId = parseRoomId(roomId);

        User sender = findUserByEmail(senderEmail);
        Room room = roomRepository.findById(parsedRoomId).orElseThrow();

        if (!isRoomMember(room, sender)) {
            throw new SecurityException("Nie należysz do tego pokoju");
        }

        if (room.getBannedUsers().contains(sender)) {
            throw new SecurityException("Jesteś zbanowany w tym pokoju");
        }

        Message message = new Message();
        message.setRoom(room);
        message.setSender(sender);
        message.setContent(normalizeContent(chatMessage.getContent()));
        message.setAttachmentUrl(chatMessage.getAttachmentUrl());
        message.setReplyToId(chatMessage.getReplyToId());
        message.setTimestamp(LocalDateTime.now());

        validateRoomReply(parsedRoomId, chatMessage.getReplyToId());

        Message saved = messageRepository.save(message);

        ChatMessage response = new ChatMessage();
        response.setType(MessageType.CHAT);
        response.setRoomId(roomId);
        response.setSender(resolveBusinessUsername(sender));
        response.setContent(saved.getContent());
        response.setAttachmentUrl(saved.getAttachmentUrl());
        response.setReplyToId(saved.getReplyToId());
        response.setTimestamp(saved.getTimestamp());

        return response;
    }

    public ChatMessage sendPrivateMessage(User sender, User receiver, ChatMessage chatMessage) {
        if (sender == null || receiver == null) {
            throw new IllegalArgumentException("Sender i receiver są wymagani");
        }

        if (!canSendPrivateMessage(sender, receiver)) {
            throw new SecurityException("Nie można wysłać wiadomości do tego użytkownika");
        }

        validatePrivateReply(sender, receiver, chatMessage.getReplyToId());

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(normalizeContent(chatMessage.getContent()));
        message.setAttachmentUrl(chatMessage.getAttachmentUrl());
        message.setReplyToId(chatMessage.getReplyToId());
        message.setTimestamp(LocalDateTime.now());

        Message saved = messageRepository.save(message);

        ChatMessage response = new ChatMessage();
        response.setType(MessageType.CHAT);
        response.setSender(resolveBusinessUsername(sender));
        response.setContent(saved.getContent());
        response.setAttachmentUrl(saved.getAttachmentUrl());
        response.setReplyToId(saved.getReplyToId());
        response.setTimestamp(saved.getTimestamp());

        return response;
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
        User currentUser = findUserByEmail(email);
        User otherUser = findUserByUsername(otherUsername);

        List<Message> conversation = getConversation(currentUser, otherUser);

        boolean areFriends = currentUser.isFriend(otherUser) && otherUser.isFriend(currentUser);
        boolean anyBanExists = isBlockedEitherWay(currentUser, otherUser);

        if (!areFriends && conversation.isEmpty()) {
            throw new SecurityException("Brak dostępu do tej rozmowy");
        }

        if (anyBanExists) {
            return conversation;
        }

        return conversation;
    }

    public void markPrivateMessagesRead(String email, String otherUsername) {
        User currentUser = findUserByEmail(email);
        User otherUser = findUserByUsername(otherUsername);

        List<Message> conversation = getConversation(currentUser, otherUser);

        LocalDateTime now = LocalDateTime.now();

        for (Message message : conversation) {
            if (otherUser.equals(message.getSender())
                    && currentUser.equals(message.getReceiver())
                    && message.getReadAt() == null) {
                message.setReadAt(now);
            }
        }
    }

    @Transactional(readOnly = true)
    public long getUnreadPrivateCount(String email) {
        User user = findUserByEmail(email);
        return messageRepository.countUnreadPrivateMessages(user);
    }

    @Transactional(readOnly = true)
    public long getUnreadRoomCount(String email) {
        User user = findUserByEmail(email);
        List<Room> accessibleRooms = getAccessibleRooms(user);

        if (accessibleRooms.isEmpty()) {
            return 0L;
        }

        return messageRepository.countUnreadRoomMessages(user, accessibleRooms);
    }

    private List<Message> getConversation(User a, User b) {
        List<Message> direct = messageRepository.findBySenderAndReceiverOrderByTimestampAsc(a, b);
        List<Message> reverse = messageRepository.findByReceiverAndSenderOrderByTimestampAsc(a, b);

        List<Message> all = new ArrayList<>(direct.size() + reverse.size());
        all.addAll(direct);
        all.addAll(reverse);
        all.sort(Comparator.comparing(Message::getTimestamp));
        return all;
    }

    private List<Room> getAccessibleRooms(User user) {
        return roomRepository.findAll().stream()
                .filter(room -> isRoomMember(room, user))
                .toList();
    }

    private boolean isRoomMember(Room room, User user) {
        return room.getOwner().equals(user)
                || room.getAdmins().contains(user)
                || room.getMembers().contains(user);
    }

    private boolean canSendPrivateMessage(User sender, User receiver) {
        return sender.isFriend(receiver)
                && receiver.isFriend(sender)
                && !sender.hasBanned(receiver)
                && !receiver.hasBanned(sender)
                && !userBanRepository.existsByBannerAndBanned(sender, receiver)
                && !userBanRepository.existsByBannerAndBanned(receiver, sender);
    }

    private boolean isBlockedEitherWay(User a, User b) {
        return a.hasBanned(b)
                || b.hasBanned(a)
                || userBanRepository.existsByBannerAndBanned(a, b)
                || userBanRepository.existsByBannerAndBanned(b, a);
    }

    private void validateRoomReply(Long roomId, Long replyToId) {
        if (replyToId == null) {
            return;
        }

        Message repliedMessage = messageRepository.findById(replyToId).orElseThrow();

        if (repliedMessage.getRoom() == null || !roomId.equals(repliedMessage.getRoom().getId())) {
            throw new IllegalArgumentException("Nieprawidłowa wiadomość do odpowiedzi");
        }
    }

    private void validatePrivateReply(User sender, User receiver, Long replyToId) {
        if (replyToId == null) {
            return;
        }

        Message repliedMessage = messageRepository.findById(replyToId).orElseThrow();

        boolean belongsToConversation =
                (sender.equals(repliedMessage.getSender()) && receiver.equals(repliedMessage.getReceiver()))
                        || (receiver.equals(repliedMessage.getSender()) && sender.equals(repliedMessage.getReceiver()));

        if (!belongsToConversation) {
            throw new IllegalArgumentException("Nieprawidłowa wiadomość do odpowiedzi");
        }
    }

    private String normalizeContent(String content) {
        if (content == null) {
            return null;
        }

        String normalized = content.trim();

        if (normalized.isEmpty()) {
            return null;
        }

        if (normalized.length() > 3072) {
            throw new IllegalArgumentException("Wiadomość przekracza maksymalny rozmiar 3 KB");
        }

        return normalized;
    }

    private Long parseRoomId(String roomId) {
        try {
            return Long.parseLong(roomId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Nieprawidłowe roomId: " + roomId);
        }
    }

    private String extractAuthenticatedEmail(SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor == null || headerAccessor.getUser() == null || headerAccessor.getUser().getName() == null) {
            throw new IllegalStateException("Brak uwierzytelnionego użytkownika");
        }

        return headerAccessor.getUser().getName().trim().toLowerCase();
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

    private String resolveBusinessUsername(User user) {
        if (user.getUsername() != null
                && !user.getUsername().isBlank()
                && !user.getUsername().equalsIgnoreCase(user.getEmail())) {
            return user.getUsername();
        }

        return userRepository.findById(user.getId())
                .map(User::getUsername)
                .orElse(user.getEmail());
    }
}
