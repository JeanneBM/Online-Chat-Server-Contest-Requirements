package pl.workshop.chatapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.workshop.chatapp.model.Message;
import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.MessageRepository;
import pl.workshop.chatapp.repository.RoomRepository;
import pl.workshop.chatapp.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;
    private final FileService fileService;

    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        roomRepository.findAll().forEach(room -> {
            room.getMembers().remove(user);
            room.getAdmins().remove(user);
        });

        for (Room room : roomRepository.findByOwner(user)) {
            messageRepository.findByRoomOrderByTimestampAsc(room).stream()
                    .map(Message::getAttachmentUrl)
                    .forEach(fileService::deleteFile);

            fileService.deleteRoomFiles(room.getId());
            roomRepository.delete(room);
        }

        userRepository.delete(user);
    }
}
