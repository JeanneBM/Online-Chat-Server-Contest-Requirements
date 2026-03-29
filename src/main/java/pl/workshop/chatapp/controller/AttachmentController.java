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

    private static final String UPLOAD_DIR = "/app/uploads/";

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Plik jest pusty");
        }
        if (file.getSize() > 20 * 1024 * 1024) { // 20 MB
            return ResponseEntity.badRequest().body("Plik za duży (max 20 MB)");
        }

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + filename);
        Files.createDirectories(path.getParent());
        file.transferTo(path);

        String url = "/uploads/" + filename;
        return ResponseEntity.ok(Map.of("url", url, "filename", file.getOriginalFilename()));
    }
}
