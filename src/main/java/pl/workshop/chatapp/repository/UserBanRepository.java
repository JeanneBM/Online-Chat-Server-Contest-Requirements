package pl.workshop.chatapp.repository;

import pl.workshop.chatapp.model.UserBan;
import pl.workshop.chatapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBanRepository extends JpaRepository<UserBan, Long> {
    boolean existsByBannerAndBanned(User banner, User banned);
}
