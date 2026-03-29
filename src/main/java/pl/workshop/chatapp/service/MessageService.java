package pl.workshop.chatapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.workshop.chatapp.model.ChatMessage;
import pl.workshop.chatapp.model.Message;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.MessageRepository;
import pl.workshop.chatapp.repository.RoomRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;

    public ChatMessage sendRoomMessage(String roomId, ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // logika z obecnego ChatController
        String username = headerAccessor.getUser().getName();
        // ... (zapis do bazy itd.)
        return chatMessage;
    }

    public ChatMessage sendPrivateMessage(User sender, User receiver, ChatMessage chatMessage) {
        Message dbMessage = new Message();
        dbMessage.setSender(sender);
        dbMessage.setReceiver(receiver);           // dodaj pole receiver w encji Message jeśli nie ma
        dbMessage.setContent(chatMessage.getContent());
        dbMessage.setAttachmentUrl(chatMessage.getAttachmentUrl());
        dbMessage.setTimestamp(LocalDateTime.now());
        messageRepository.save(dbMessage);

        chatMessage.setSender(sender.getUsername());
        chatMessage.setTimestamp(LocalDateTime.now());
        return chatMessage;
    }
}
