package pl.workshop.chatapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.RoomRepository;
import pl.workshop.chatapp.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final FileService fileService;

    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        // Usuwamy wszystkie pokoje, których jest ownerem + pliki
        roomRepository.findByOwner(user).forEach(room -> {
            fileService.deleteRoomFiles(room.getId());
            roomRepository.delete(room);
        });

        userRepository.delete(user);
    }
}
