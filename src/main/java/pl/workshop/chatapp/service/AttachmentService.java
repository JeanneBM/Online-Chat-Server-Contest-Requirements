package pl.workshop.chatapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.workshop.chatapp.model.Attachment;
import pl.workshop.chatapp.model.User;
import pl.workshop.chatapp.repository.AttachmentRepository;
import pl.workshop.chatapp.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AttachmentService {

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;
    private static final long MAX_IMAGE_SIZE = 3 * 1024 * 1024;

    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public Map<String, Object> upload(MultipartFile file, String email) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Plik jest pusty");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Brak zalogowanego użytkownika");
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase()).orElseThrow();

        String contentType = file.getContentType() != null ? file.getContentType() : "";
        boolean image = contentType.startsWith("image/");
        long maxAllowed = image ? MAX_IMAGE_SIZE : MAX_FILE_SIZE;

        if (file.getSize() > maxAllowed) {
            throw new IllegalArgumentException(image
                    ? "Obraz za duży (max 3 MB)"
                    : "Plik za duży (max 20 MB)");
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String storedFilename = UUID.randomUUID() + "_" + Paths.get(originalFilename).getFileName();
        Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path path = uploadRoot.resolve(storedFilename).normalize();

        Files.createDirectories(uploadRoot);
        file.transferTo(path);

        String url = "/uploads/" + storedFilename;

        Attachment attachment = new Attachment();
        attachment.setUploadedBy(user);
        attachment.setOriginalFilename(originalFilename);
        attachment.setStoredFilename(storedFilename);
        attachment.setContentType(contentType);
        attachment.setSize(file.getSize());
        attachment.setUrl(url);
        attachmentRepository.save(attachment);

        return Map.of(
                "id", attachment.getId(),
                "url", url,
                "filename", originalFilename,
                "contentType", contentType,
                "size", file.getSize()
        );
    }
}
