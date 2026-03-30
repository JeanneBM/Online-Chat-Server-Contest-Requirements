package pl.workshop.chatapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.workshop.chatapp.model.Message;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.RoomBan;
import pl.workshop.chatapp.model.RoomType;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.MessageRepository;
import pl.workshop.chatapp.repository.RoomBanRepository;
import pl.workshop.chatapp.repository.RoomRepository;
import pl.workshop.chatapp.repository.UserRepository;

import java.time.LocalDateTime;
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
    private final RoomBanRepository roomBanRepository;

    public List<Room> getPublicRooms(String search) {
        List<Room> publicRooms = roomRepository.findByType(RoomType.PUBLIC);

        if (search == null || search.isBlank()) {
            return publicRooms;
        }

        String needle = search.trim().toLowerCase();

        return publicRooms.stream()
                .filter(room ->
                        (room.getName() != null && room.getName().toLowerCase().contains(needle)) ||
                        (room.getDescription() != null && room.getDescription().toLowerCase().contains(needle)))
                .toList();
    }

    public Room joinPublicRoom(Long roomId, String email) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User user = findUserByEmail(email);

        if (room.getType() != RoomType.PUBLIC) {
            throw new IllegalStateException("Do pokoju prywatnego można wejść tylko przez zaproszenie");
        }

        if (room.getBannedUsers().contains(user)) {
            throw new IllegalStateException("Jesteś zbanowany w tym pokoju");
        }

        room.getMembers().add(user);
        return roomRepository.save(room);
    }

    public Room leaveRoom(Long roomId, String email) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User user = findUserByEmail(email);

        if (room.getOwner().equals(user)) {
            throw new IllegalStateException("Owner nie może opuścić własnego pokoju");
        }

        room.getMembers().remove(user);
        room.getAdmins().remove(user);

        return roomRepository.save(room);
    }

    public void deleteRoom(Long roomId, String email) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User actingUser = findUserByEmail(email);

        if (!room.getOwner().equals(actingUser)) {
            throw new SecurityException("Tylko owner może usunąć pokój");
        }

        messageRepository.findByRoomOrderByTimestampAsc(room).stream()
                .map(Message::getAttachmentUrl)
                .forEach(fileService::deleteFile);

        fileService.deleteRoomFiles(roomId);
        roomRepository.delete(room);
    }

    public void deleteMessage(Long roomId, Long messageId, String email) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User actingUser = findUserByEmail(email);

        if (!isAdminOrOwner(room, actingUser)) {
            throw new SecurityException("Tylko owner lub admin może usuwać wiadomości");
        }

        Message message = messageRepository.findByIdAndRoom(messageId, room).orElseThrow();

        fileService.deleteFile(message.getAttachmentUrl());
        messageRepository.delete(message);
    }

    public Room banUserFromRoom(Long roomId, String targetUsername, String actingEmail) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User actingUser = findUserByEmail(actingEmail);
        User targetUser = findUserByUsername(targetUsername);

        if (!isAdminOrOwner(room, actingUser)) {
            throw new SecurityException("Tylko owner lub admin może banować użytkowników");
        }

        if (room.getOwner().equals(targetUser)) {
            throw new IllegalStateException("Nie można zbanować ownera pokoju");
        }

        room.getMembers().remove(targetUser);
        room.getAdmins().remove(targetUser);
        room.getBannedUsers().add(targetUser);

        roomBanRepository.findByRoomAndBannedUser(room, targetUser)
                .ifPresentOrElse(existing -> {
                    existing.setBannedByUser(actingUser);
                    existing.setBannedAt(LocalDateTime.now());
                }, () -> {
                    RoomBan roomBan = new RoomBan();
                    roomBan.setRoom(room);
                    roomBan.setBannedUser(targetUser);
                    roomBan.setBannedByUser(actingUser);
                    roomBan.setBannedAt(LocalDateTime.now());
                    roomBanRepository.save(roomBan);
                });

        return roomRepository.save(room);
    }

    public Room removeMember(Long roomId, String targetUsername, String actingEmail) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User actingUser = findUserByEmail(actingEmail);
        User targetUser = findUserByUsername(targetUsername);

        if (!isAdminOrOwner(room, actingUser)) {
            throw new SecurityException("Tylko owner lub admin może usuwać członków");
        }

        if (room.getOwner().equals(targetUser)) {
            throw new IllegalStateException("Nie można usunąć ownera pokoju");
        }

        room.getMembers().remove(targetUser);
        room.getAdmins().remove(targetUser);
        room.getBannedUsers().add(targetUser);

        roomBanRepository.findByRoomAndBannedUser(room, targetUser)
                .ifPresentOrElse(existing -> {
                    existing.setBannedByUser(actingUser);
                    existing.setBannedAt(LocalDateTime.now());
                }, () -> {
                    RoomBan roomBan = new RoomBan();
                    roomBan.setRoom(room);
                    roomBan.setBannedUser(targetUser);
                    roomBan.setBannedByUser(actingUser);
                    roomBan.setBannedAt(LocalDateTime.now());
                    roomBanRepository.save(roomBan);
                });

        return roomRepository.save(room);
    }

    public Room unbanUser(Long roomId, String targetUsername, String actingEmail) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User actingUser = findUserByEmail(actingEmail);
        User targetUser = findUserByUsername(targetUsername);

        if (!isAdminOrOwner(room, actingUser)) {
            throw new SecurityException("Tylko owner lub admin może odbanować użytkowników");
        }

        room.getBannedUsers().remove(targetUser);
        roomBanRepository.deleteByRoomAndBannedUser(room, targetUser);
        return roomRepository.save(room);
    }

    @Transactional(readOnly = true)
    public Set<User> getBannedUsers(Long roomId, String actingEmail) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User actingUser = findUserByEmail(actingEmail);

        if (!isAdminOrOwner(room, actingUser)) {
            throw new SecurityException("Tylko owner lub admin może zobaczyć listę banów");
        }

        return room.getBannedUsers();
    }

    @Transactional(readOnly = true)
    public List<RoomBan> getRoomBanDetails(Long roomId, String actingEmail) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User actingUser = findUserByEmail(actingEmail);

        if (!isAdminOrOwner(room, actingUser)) {
            throw new SecurityException("Tylko owner lub admin może zobaczyć szczegóły banów");
        }

        return roomBanRepository.findByRoomOrderByBannedAtDesc(room);
    }

    public void markRoomMessagesRead(Long roomId, String email) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User user = findUserByEmail(email);

        List<Message> messages = messageRepository.findByRoomOrderByTimestampAsc(room);
        LocalDateTime now = LocalDateTime.now();

        messages.stream()
                .filter(message -> !user.equals(message.getSender()) && message.getReadAt() == null)
                .forEach(message -> message.setReadAt(now));
    }

    public boolean isMember(Room room, User user) {
        return room.getOwner().equals(user)
                || room.getMembers().contains(user)
                || room.getAdmins().contains(user);
    }

    private boolean isAdminOrOwner(Room room, User user) {
        return room.getOwner().equals(user) || room.getAdmins().contains(user);
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
}
