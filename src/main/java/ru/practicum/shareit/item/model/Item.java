package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.Data;
import ru.practicum.shareit.user.User;

@Data
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "available", nullable = false)
    private boolean available;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}

