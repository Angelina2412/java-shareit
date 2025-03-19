package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.BookerDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    private Long id;
    private Long itemId;
    private BookerDto booker;
    @NotNull(message = "Дата начала бронирования не может быть пустой")
    private LocalDateTime start;
    @NotNull(message = "Дата окончания бронирования не может быть пустой")
    private LocalDateTime end;
    private BookingStatus status;

    public BookingDto(Long id, Long bookerId) {
        this.id = id;
        this.booker = new BookerDto(bookerId);
    }
}




