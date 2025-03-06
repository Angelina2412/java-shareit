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

    BookingDto getBookingById(Long bookingId, Long userId)
            throws NotFoundException, AccessDeniedException;

    List<BookingDto> getUserBookings(Long userId, String state);

    List<BookingDto> getOwnerBookings(Long ownerId, String state);
}


