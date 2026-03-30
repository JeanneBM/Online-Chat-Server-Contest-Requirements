package pl.workshop.chatapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.workshop.chatapp.model.Message;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.RoomType;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.MessageRepository;
import pl.workshop.chatapp.repository.RoomRepository;
import pl.workshop.chatapp.repository.UserRepository;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final FileService fileService;

    public List<Room> getPublicRooms(String search) {
        List<Room> publicRooms = roomRepository.findByType(RoomType.PUBLIC);
        if (search == null || search.isBlank()) {
            return publicRooms;
        }

        String needle = search.toLowerCase();
        return publicRooms.stream()
                .filter(room -> (room.getName() != null && room.getName().toLowerCase().contains(needle))
                        || (room.getDescription() != null && room.getDescription().toLowerCase().contains(needle)))
                .toList();
    }

    public Room joinPublicRoom(Long roomId, String username) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();

        if (room.getType() != RoomType.PUBLIC) {
            throw new IllegalStateException("Do pokoju prywatnego można wejść tylko przez zaproszenie");
        }
        if (room.getBannedUsers().contains(user)) {
            throw new IllegalStateException("Jesteś zbanowany w tym pokoju");
        }

        room.getMembers().add(user);
        return roomRepository.save(room);
    }

    public Room leaveRoom(Long roomId, String username) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();

        if (room.getOwner().equals(user)) {
            throw new IllegalStateException("Owner nie może opuścić własnego pokoju");
        }

        room.getMembers().remove(user);
        room.getAdmins().remove(user);
        return roomRepository.save(room);
    }

    public void deleteRoom(Long roomId, String username) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        if (!room.getOwner().getUsername().equals(username)) {
            throw new SecurityException("Tylko owner może usunąć pokój");
        }

        messageRepository.findByRoomOrderByTimestampAsc(room).stream()
                .map(Message::getAttachmentUrl)
                .forEach(fileService::deleteFile);
        fileService.deleteRoomFiles(roomId);
        roomRepository.delete(room);
    }

    public void deleteMessage(Long roomId, Long messageId, String username) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User actingUser = userRepository.findByUsername(username).orElseThrow();

        if (!isAdminOrOwner(room, actingUser)) {
            throw new SecurityException("Tylko owner lub admin może usuwać wiadomości");
        }

        Message message = messageRepository.findByIdAndRoom(messageId, room).orElseThrow();
        fileService.deleteFile(message.getAttachmentUrl());
        messageRepository.delete(message);
    }

    public Room banUserFromRoom(Long roomId, String targetUsername, String actingUsername) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User actingUser = userRepository.findByUsername(actingUsername).orElseThrow();
        User targetUser = userRepository.findByUsername(targetUsername.trim()).orElseThrow();

        if (!isAdminOrOwner(room, actingUser)) {
            throw new SecurityException("Tylko owner lub admin może banować użytkowników");
        }
        if (room.getOwner().equals(targetUser)) {
            throw new IllegalStateException("Nie można zbanować ownera pokoju");
        }

        room.getMembers().remove(targetUser);
        room.getAdmins().remove(targetUser);
        room.getBannedUsers().add(targetUser);
        return roomRepository.save(room);
    }

    public Room removeMember(Long roomId, String targetUsername, String actingUsername) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User actingUser = userRepository.findByUsername(actingUsername).orElseThrow();
        User targetUser = userRepository.findByUsername(targetUsername.trim()).orElseThrow();

        if (!isAdminOrOwner(room, actingUser)) {
            throw new SecurityException("Tylko owner lub admin może usuwać członków");
        }
        if (room.getOwner().equals(targetUser)) {
            throw new IllegalStateException("Nie można usunąć ownera pokoju");
        }

        room.getMembers().remove(targetUser);
        room.getAdmins().remove(targetUser);
        room.getBannedUsers().add(targetUser);
        return roomRepository.save(room);
    }

    public Room unbanUser(Long roomId, String targetUsername, String actingUsername) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User actingUser = userRepository.findByUsername(actingUsername).orElseThrow();
        User targetUser = userRepository.findByUsername(targetUsername.trim()).orElseThrow();

        if (!isAdminOrOwner(room, actingUser)) {
            throw new SecurityException("Tylko owner lub admin może odbanować użytkowników");
        }

        room.getBannedUsers().remove(targetUser);
        return roomRepository.save(room);
    }

    public Set<User> getBannedUsers(Long roomId, String actingUsername) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User actingUser = userRepository.findByUsername(actingUsername).orElseThrow();

        if (!isAdminOrOwner(room, actingUser)) {
            throw new SecurityException("Tylko owner lub admin może zobaczyć listę banów");
        }
        return room.getBannedUsers();
    }

    public void markRoomMessagesRead(Long roomId, String username) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();
        List<Message> messages = messageRepository.findByRoomOrderByTimestampAsc(room);
        messages.stream()
                .filter(m -> !user.equals(m.getSender()) && m.getReadAt() == null)
                .forEach(m -> m.setReadAt(java.time.LocalDateTime.now()));
    }

    public boolean isMember(Room room, User user) {
        return room.getOwner().equals(user) || room.getMembers().contains(user) || room.getAdmins().contains(user);
    }

    private boolean isAdminOrOwner(Room room, User user) {
        return room.getOwner().equals(user) || room.getAdmins().contains(user);
    }
}
