package pl.workshop.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.workshop.chatapp.model.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}
