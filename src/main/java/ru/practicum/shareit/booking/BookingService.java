package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exceptions.NotFoundException;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface BookingService {

    BookingResponseDto addBooking(Long userId, BookingDto bookingDto);

    BookingResponseDto updateBookingStatus(Long bookingId, Long ownerId, boolean approved)
            throws NotFoundException;

    BookingResponseDto getBookingById(Long bookingId, Long userId)
            throws NotFoundException, AccessDeniedException;

    List<BookingResponseDto> getUserBookings(Long userId, String state);

    List<BookingResponseDto> getOwnerBookings(Long ownerId, String state);
}



