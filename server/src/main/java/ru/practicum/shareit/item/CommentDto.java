package ru.practicum.shareit.item;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CommentDto {

    private Long id;
    private String text;
    private Long userId;
    private String authorName;
    private LocalDateTime created;

    public CommentDto(String text) {
        this.text = text;
    }

    public CommentDto(Long id, String text, Long userId, String authorName, LocalDateTime created) {
        this.id = id;
        this.text = text;
        this.userId = userId;
        this.authorName = authorName;
        this.created = created;
    }
}
