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

}
