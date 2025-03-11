package ru.practicum.shareit.item;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;
    private String text;
    private Long userId;
    private String authorName;
    private LocalDateTime created;

    public CommentDto(Long id, String text, Long userId, String authorName, LocalDateTime created) {
        this.id = id;
        this.text = text;
        this.userId = userId;
        this.authorName = authorName;
        this.created = created;
    }

    public CommentDto(){

    }
}
