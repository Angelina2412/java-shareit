package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBooking(@Valid @RequestBody BookingDto bookingDto,
                                                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.addBooking(userId, bookingDto);  // Получаем ResponseEntity из сервиса
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateBookingStatus(@PathVariable Long id,
                                                                   @RequestParam boolean approved,
                                                                   @RequestHeader("X-Sharer-User-Id") Long ownerId) throws AccessDeniedException {
        return bookingService.updateBookingStatus(id, ownerId, approved);  // Получаем ResponseEntity с обновленным бронированием
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDto> getBooking(@PathVariable Long id,
                                                 @RequestHeader("X-Sharer-User-Id") Long userId) throws AccessDeniedException {
        BookingDto booking = bookingService.getBookingById(id, userId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping
    public List<BookingDto> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getUserBookings(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                             @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getOwnerBookings(ownerId, state);
    }
}


