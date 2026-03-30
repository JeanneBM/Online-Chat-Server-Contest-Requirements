package pl.workshop.chatapp.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

@Service
public class FileService {

    public void deleteRoomFiles(Long roomId) {
        Path roomDir = Paths.get("./uploads/room-" + roomId);
        if (Files.notExists(roomDir) || !Files.isDirectory(roomDir)) {
            return;
        }

        try (var walk = Files.walk(roomDir)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        String normalized = fileUrl.startsWith("/uploads/")
                ? "." + fileUrl
                : fileUrl;

        try {
            Files.deleteIfExists(Paths.get(normalized));
        } catch (IOException ignored) {
        }
    }
}
