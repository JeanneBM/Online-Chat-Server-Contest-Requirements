package pl.workshop.chatapp.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pl.workshop.chatapp.service.AttachmentService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

@RestController
public class DownloadController {
    private final AttachmentService attachmentService;

    public DownloadController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping("/uploads/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename, Principal principal) throws IOException {
        Resource resource = attachmentService.download(filename, principal.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8).build().toString())
                .body(resource);
    }
}
