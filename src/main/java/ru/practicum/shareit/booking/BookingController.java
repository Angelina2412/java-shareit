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

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@Valid @RequestBody BookingDto bookingDto,
                                                    @RequestHeader("X-Sharer-User-Id") Long userId) {
        BookingDto createdBooking = bookingService.addBooking(userId, bookingDto);
        return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);
    }


    @PatchMapping("/{id}")
    public ResponseEntity<BookingDto> updateBookingStatus(@PathVariable Long id,
                                                          @RequestParam boolean approved,
                                                          @RequestHeader("X-User-Id") Long ownerId) throws AccessDeniedException {
        BookingDto updatedBooking = bookingService.updateBookingStatus(id, ownerId, approved);
        return ResponseEntity.ok(updatedBooking);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDto> getBooking(@PathVariable Long id,
                                                 @RequestHeader("X-User-Id") Long userId) throws AccessDeniedException {
        BookingDto booking = bookingService.getBookingById(id, userId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping
    public List<BookingDto> getUserBookings(@RequestHeader("X-User-Id") Long userId,
                                            @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getUserBookings(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestHeader("X-User-Id") Long ownerId,
                                             @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getOwnerBookings(ownerId, state);
    }
}


