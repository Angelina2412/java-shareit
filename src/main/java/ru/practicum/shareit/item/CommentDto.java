package ru.practicum.shareit.item;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;
    private String text;
    private Long userId;
    private String userName;
    private LocalDateTime createdDate;

    public CommentDto(Long id, String text, Long userId, String userName, LocalDateTime createdDate) {
        this.id = id;
        this.text = text;
        this.userId = userId;
        this.userName = userName;
        this.createdDate = createdDate;
    }

    public CommentDto(){

    }
}
