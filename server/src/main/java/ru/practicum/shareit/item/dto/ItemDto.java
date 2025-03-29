package ru.practicum.shareit.item.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.CommentDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {

    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private LocalDateTime lastBooking;
    private LocalDateTime nextBooking;

    private List<CommentDto> comments;

    private Long requestId;

    public ItemDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}

