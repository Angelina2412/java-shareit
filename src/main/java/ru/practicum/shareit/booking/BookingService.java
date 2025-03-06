package ru.practicum.shareit.booking;

import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exceptions.NotFoundException;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

public interface BookingService {

    ResponseEntity<Map<String, Object>> addBooking(Long userId, BookingDto bookingDto);

    ResponseEntity<Map<String, Object>> updateBookingStatus(Long bookingId, Long ownerId, boolean approved)
            throws NotFoundException, AccessDeniedException;

    ResponseEntity<Map<String, Object>> getBookingById(Long bookingId, Long userId)
            throws NotFoundException, AccessDeniedException;

    ResponseEntity<List<Map<String, Object>>> getUserBookings(Long userId, String state);

    ResponseEntity<List<Map<String, Object>>> getOwnerBookings(Long ownerId, String state);

}


