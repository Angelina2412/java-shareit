package ru.practicum.shareit.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(@RequestBody BookingDto bookingDto,
                                                            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return ResponseEntity.ok(bookingService.addBooking(userId, bookingDto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookingResponseDto> updateBookingStatus(@PathVariable Long id,
                                                                  @RequestParam boolean approved,
                                                                  @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(id, ownerId, approved));
    }


    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDto> getBooking(@PathVariable Long id,
                                                         @RequestHeader("X-Sharer-User-Id") Long userId)
            throws AccessDeniedException {
        return ResponseEntity.ok(bookingService.getBookingById(id, userId));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                    @RequestParam(defaultValue = "ALL") String state) {
        return ResponseEntity.ok(bookingService.getUserBookings(userId, state));
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingResponseDto>> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                                     @RequestParam(defaultValue = "ALL") String state) {
        return ResponseEntity.ok(bookingService.getOwnerBookings(ownerId, state));
    }
}


