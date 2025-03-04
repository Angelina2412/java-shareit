package ru.practicum.shareit.item.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.item.CommentDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemDto {
    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    private String name;

    @NotBlank(message = "Описание не может быть пустым")
    private String description;

    @NotNull(message = "Доступность должна быть указана")
    private Boolean available;

    private LocalDateTime lastBookingStartDate;
    private LocalDateTime nextBookingStartDate;

    private List<CommentDto> comments;

    public ItemDto(){

    }

    public ItemDto(Long id, String name, String description, Boolean available, LocalDateTime lastBookingStartDate,
                   LocalDateTime nextBookingStartDate, List<CommentDto> comments) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.lastBookingStartDate = lastBookingStartDate;
        this.nextBookingStartDate = nextBookingStartDate;
        this.comments = comments;
    }
}

