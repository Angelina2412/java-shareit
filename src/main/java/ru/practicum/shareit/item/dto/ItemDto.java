package ru.practicum.shareit.item.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemDto {
    private Long id;
    @NotBlank(message = "name не может быть пустым")
    private String name;
    @NotBlank(message = "description не может быть пустым")
    private String description;
    @NotNull(message = "Доступность должна быть указана")
    private Boolean available;
}
