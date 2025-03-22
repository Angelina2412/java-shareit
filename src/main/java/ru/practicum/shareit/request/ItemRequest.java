package ru.practicum.shareit.request;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "item_requests")
public class ItemRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description; // Текст запроса

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester; // Пользователь, который сделал запрос

    @Column(nullable = false, updatable = false)
    private LocalDateTime created; // Дата и время создания запроса

    // Важно автоматически задавать время создания
    @PrePersist
    public void prePersist() {
        created = LocalDateTime.now();
    }

    // Связь с вещами, которые могут быть предложены в ответ на запрос
    @OneToMany(mappedBy = "itemRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Item> items = new ArrayList<>();
}

