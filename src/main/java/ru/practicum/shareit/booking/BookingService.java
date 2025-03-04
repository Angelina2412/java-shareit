package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exceptions.NotFoundException;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface BookingService {

    BookingDto addBooking(Long userId, BookingDto bookingDto);

    BookingDto updateBookingStatus(Long bookingId, Long ownerId, boolean approved)
            throws NotFoundException, AccessDeniedException;

    BookingDto getBookingById(Long bookingId, Long userId)
            throws NotFoundException, AccessDeniedException;

    List<BookingDto> getUserBookings(Long userId, String state);

    List<BookingDto> getOwnerBookings(Long ownerId, String state);
}


