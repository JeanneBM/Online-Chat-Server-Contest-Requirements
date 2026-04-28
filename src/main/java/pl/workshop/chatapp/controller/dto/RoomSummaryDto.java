package pl.workshop.chatapp.controller.dto;

import pl.workshop.chatapp.model.Room;
import pl.workshop.chatapp.model.RoomType;

public record RoomSummaryDto(
        Long id,
        String name,
        String description,
        RoomType type
) {
    public static RoomSummaryDto from(Room room) {
        return new RoomSummaryDto(
                room.getId(),
                room.getName(),
                room.getDescription(),
                room.getType()
        );
    }
}
