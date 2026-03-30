package pl.workshop.chatapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.workshop.chatapp.model.ChatMessage;
import pl.workshop.chatapp.model.Message;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.MessageRepository;
import pl.workshop.chatapp.repository.RoomRepository;
import pl.workshop.chatapp.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomService roomService;

    public ChatMessage sendRoomMessage(String roomId, ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        User sender = userRepository.findByUsername(username).orElseThrow();
        Room room = roomRepository.findById(Long.valueOf(roomId)).orElseThrow();

        if (!roomService.isMember(room, sender)) {
            throw new IllegalStateException("Nie należysz do tego pokoju");
        }
        validateMessage(chatMessage);

        Message dbMessage = new Message();
        dbMessage.setRoom(room);
        dbMessage.setSender(sender);
        dbMessage.setContent(chatMessage.getContent());
        dbMessage.setAttachmentUrl(chatMessage.getAttachmentUrl());
        dbMessage.setReplyToId(chatMessage.getReplyToId());
        dbMessage.setTimestamp(LocalDateTime.now());
        messageRepository.save(dbMessage);

        chatMessage.setSender(sender.getUsername());
        chatMessage.setTimestamp(dbMessage.getTimestamp());
        chatMessage.setRoomId(roomId);
        return chatMessage;
    }

    public ChatMessage sendPrivateMessage(User sender, User receiver, ChatMessage chatMessage) {
        validateMessage(chatMessage);

        Message dbMessage = new Message();
        dbMessage.setSender(sender);
        dbMessage.setReceiver(receiver);
        dbMessage.setContent(chatMessage.getContent());
        dbMessage.setAttachmentUrl(chatMessage.getAttachmentUrl());
        dbMessage.setReplyToId(chatMessage.getReplyToId());
        dbMessage.setTimestamp(LocalDateTime.now());
        messageRepository.save(dbMessage);

        chatMessage.setSender(sender.getUsername());
        chatMessage.setTimestamp(dbMessage.getTimestamp());
        return chatMessage;
    }

    public List<Message> getRoomMessages(Long roomId, String username) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();
        if (!roomService.isMember(room, user)) {
            throw new SecurityException("Brak dostępu do historii tego pokoju");
        }
        return messageRepository.findByRoomOrderByTimestampAsc(room);
    }

    public List<Message> getPrivateMessages(String username, String otherUsername) {
        User user = userRepository.findByUsername(username).orElseThrow();
        User other = userRepository.findByUsername(otherUsername).orElseThrow();

        List<Message> result = new ArrayList<>();
        result.addAll(messageRepository.findBySenderAndReceiverOrderByTimestampAsc(user, other));
        result.addAll(messageRepository.findByReceiverAndSenderOrderByTimestampAsc(user, other));
        result.sort(Comparator.comparing(Message::getTimestamp));
        return result;
    }

    public void markPrivateMessagesRead(String username, String otherUsername) {
        User user = userRepository.findByUsername(username).orElseThrow();
        User other = userRepository.findByUsername(otherUsername).orElseThrow();
        messageRepository.findBySenderAndReceiverOrderByTimestampAsc(other, user).stream()
                .filter(m -> m.getReadAt() == null)
                .forEach(m -> m.setReadAt(LocalDateTime.now()));
    }

    public long getUnreadPrivateCount(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return messageRepository.countUnreadPrivateMessages(user);
    }

    public long getUnreadRoomCount(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        List<Room> rooms = roomRepository.findAll().stream()
                .filter(room -> roomService.isMember(room, user))
                .toList();
        if (rooms.isEmpty()) {
            return 0L;
        }
        return messageRepository.countUnreadRoomMessages(user, rooms);
    }

    private void validateMessage(ChatMessage chatMessage) {
        String content = chatMessage.getContent();
        String attachmentUrl = chatMessage.getAttachmentUrl();
        boolean hasText = content != null && !content.isBlank();
        boolean hasAttachment = attachmentUrl != null && !attachmentUrl.isBlank();

        if (!hasText && !hasAttachment) {
            throw new IllegalArgumentException("Wiadomość musi zawierać tekst lub załącznik");
        }
        if (hasText && content.getBytes(StandardCharsets.UTF_8).length > 3072) {
            throw new IllegalArgumentException("Maksymalny rozmiar tekstu wiadomości to 3 KB");
        }
    }
}
