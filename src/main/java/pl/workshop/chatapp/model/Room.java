package pl.workshop.chatapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private RoomType type; // PUBLIC lub PRIVATE

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToMany
    @JoinTable(name = "room_members",
               joinColumns = @JoinColumn(name = "room_id"),
               inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> members = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "room_banned",
               joinColumns = @JoinColumn(name = "room_id"),
               inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> bannedUsers = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "room_admins",
               joinColumns = @JoinColumn(name = "room_id"),
               inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> admins = new HashSet<>();
}

public enum RoomType {
    PUBLIC, PRIVATE
}
