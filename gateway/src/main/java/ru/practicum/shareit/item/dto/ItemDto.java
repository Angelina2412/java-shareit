package ru.practicum.shareit.item.dto;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank(message = "Имя не может быть пустым")
    private String name;
    @NotNull(message = "Описание не может быть пустым")
    private String description;
    @NotNull(message = "Доступность должна быть указана")
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

