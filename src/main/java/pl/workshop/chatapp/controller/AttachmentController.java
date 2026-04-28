package pl.workshop.chatapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.workshop.chatapp.service.AttachmentService;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file, Principal principal) throws IOException {
        return ResponseEntity.ok(attachmentService.upload(file, principal.getName()));
    }
}
