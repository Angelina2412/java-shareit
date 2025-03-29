package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

@Controller
@RequestMapping(path = "/bookings")
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    public BookingController(BookingClient bookingClient) {
        this.bookingClient = bookingClient;
    }

    @PostMapping
    public BookingResponseDto createBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                            @RequestBody @Valid BookingDto bookingDto) {
        log.info("Creating booking {}, userId={}", bookingDto, userId);
        return bookingClient.createBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto updateBookingStatus(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                  @PathVariable Long bookingId,
                                                  @RequestParam boolean approved) {
        log.info("Updating booking status, bookingId={}, ownerId={}, approved={}", bookingId, ownerId, approved);
        return bookingClient.updateBookingStatus(bookingId, ownerId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @PathVariable Long bookingId) {
        log.info("Getting booking {}, userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                  @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                  @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                  @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Getting user bookings, userId={}, state={}, from={}, size={}", userId, state, from, size);
        List<BookingResponseDto> bookings = bookingClient.getUserBookings(userId, state, from, size);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                   @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                   @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                   @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Getting owner bookings, ownerId={}, state={}, from={}, size={}", ownerId, state, from, size);
        List<BookingResponseDto> bookings = bookingClient.getOwnerBookings(ownerId, state, from, size);
        return ResponseEntity.ok(bookings);
    }

}


