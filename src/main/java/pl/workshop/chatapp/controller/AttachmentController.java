package pl.workshop.chatapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private static final Path UPLOAD_DIR = Paths.get("./uploads").toAbsolutePath().normalize();
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;
    private static final long MAX_IMAGE_SIZE = 3 * 1024 * 1024;

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Plik jest pusty");
        }

        String contentType = file.getContentType() != null ? file.getContentType() : "";
        boolean image = contentType.startsWith("image/");
        long maxAllowed = image ? MAX_IMAGE_SIZE : MAX_FILE_SIZE;

        if (file.getSize() > maxAllowed) {
            return ResponseEntity.badRequest().body(image
                    ? "Obraz za duży (max 3 MB)"
                    : "Plik za duży (max 20 MB)");
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String filename = UUID.randomUUID() + "_" + Paths.get(originalFilename).getFileName();
        Path path = UPLOAD_DIR.resolve(filename).normalize();

        Files.createDirectories(UPLOAD_DIR);
        file.transferTo(path);

        String url = "/uploads/" + filename;
        return ResponseEntity.ok(Map.of(
                "url", url,
                "filename", originalFilename,
                "contentType", contentType,
                "size", file.getSize()
        ));
    }
}
