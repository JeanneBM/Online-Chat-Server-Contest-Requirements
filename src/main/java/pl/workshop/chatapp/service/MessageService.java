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

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public ChatMessage sendRoomMessage(String roomId, ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        User sender = userRepository.findByUsername(username).orElseThrow();
        Room room = roomRepository.findById(Long.valueOf(roomId)).orElseThrow();

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
}
