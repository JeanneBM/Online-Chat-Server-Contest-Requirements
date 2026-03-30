package pl.workshop.chatapp.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

@Service
public class FileService {

    private static final Path UPLOAD_ROOT = Paths.get("./uploads").toAbsolutePath().normalize();

    public void deleteRoomFiles(Long roomId) {
        Path roomDir = UPLOAD_ROOT.resolve("room-" + roomId).normalize();
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
        Path resolved = resolveUploadPath(fileUrl);
        if (resolved == null) {
            return;
        }

        try {
            Files.deleteIfExists(resolved);
        } catch (IOException ignored) {
        }
    }

    private Path resolveUploadPath(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return null;
        }

        String normalizedUrl = fileUrl.trim().replace('\\', '/');
        if (normalizedUrl.startsWith("/uploads/")) {
            normalizedUrl = normalizedUrl.substring("/uploads/".length());
            return UPLOAD_ROOT.resolve(normalizedUrl).normalize();
        }

        Path rawPath = Paths.get(normalizedUrl).normalize();
        if (rawPath.isAbsolute()) {
            return rawPath;
        }

        return rawPath.toAbsolutePath().normalize();
    }
}
