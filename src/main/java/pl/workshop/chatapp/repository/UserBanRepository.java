package com.example.chatserver.repository;

import com.example.chatserver.model.UserBan;
import com.example.chatserver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBanRepository extends JpaRepository<UserBan, Long> {
    boolean existsByBannerAndBanned(User banner, User banned);
}
