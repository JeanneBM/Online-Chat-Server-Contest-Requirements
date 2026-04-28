package pl.workshop.chatapp.controller.dto;

import pl.workshop.chatapp.model.PresenceStatus;
import pl.workshop.chatapp.model.User;

public record FriendDto(Long id, String username, String email, String presenceStatus) {
    public static FriendDto from(User user) {
        PresenceStatus status = user.getPresenceStatus() != null ? user.getPresenceStatus() : PresenceStatus.OFFLINE;
        return new FriendDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                status.name()
        );
    }
}
