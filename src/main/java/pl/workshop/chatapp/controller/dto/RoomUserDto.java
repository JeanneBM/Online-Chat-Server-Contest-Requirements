package pl.workshop.chatapp.controller.dto;

import pl.workshop.chatapp.model.PresenceStatus;
import pl.workshop.chatapp.model.User;

public record RoomUserDto(Long id, String username, String presenceStatus, boolean owner, boolean admin) {
    public static RoomUserDto from(User user, boolean owner, boolean admin) {
        PresenceStatus status = user.getPresenceStatus() != null ? user.getPresenceStatus() : PresenceStatus.OFFLINE;
        return new RoomUserDto(user.getId(), user.getUsername(), status.name(), owner, admin);
    }
}
