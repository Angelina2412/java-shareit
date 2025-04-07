package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

    private final BookingClient bookingClient;


    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @RequestBody @Valid BookingDto requestDto) {
        log.info("Creating booking {}, userId={}", requestDto, userId);
        return bookingClient.createBooking(userId, requestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateBookingStatus(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                      @PathVariable Long bookingId,
                                                      @RequestParam boolean approved) {
        log.info("PATCH /bookings/{} — updateBookingStatus called by ownerId={}, approved={}", bookingId, ownerId, approved);
        return bookingClient.updateBookingStatus(bookingId, ownerId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable Long bookingId) {
        log.info("GET /bookings/{} — getBooking called by userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                  @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                  @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                  @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("GET /bookings — getUserBookings called by userId={}, state={}, from={}, size={}", userId, state, from, size);
        return bookingClient.getUserBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                   @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                   @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                   @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("GET /bookings/owner — getOwnerBookings called by ownerId={}, state={}, from={}, size={}", ownerId, state, from, size);
        return bookingClient.getOwnerBookings(ownerId, state, from, size);
    }

    //    @PostMapping
//    public ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id") long userId,
//                                                @RequestBody @Valid BookingDto bookingDto) {
//        log.info("POST /bookings — createBooking called by userId={} with body={}", userId, bookingDto);
//        ResponseEntity<Object> response = bookingClient.createBooking(userId, bookingDto);
//        log.info("POST /bookings — response: status={}, body={}", response.getStatusCode(), response.getBody());
//        return response;
//    }

}



