package pl.workshop.chatapp.service;

import pl.workshop.chatapp.model.PresenceStatus;

public record PresenceStatusChangedEvent(String username, PresenceStatus status) {
}
