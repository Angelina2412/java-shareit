package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.client.BaseClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingClient {

    private final BaseClient baseClient;
    private static final String API_PREFIX = "/bookings";

    public BookingResponseDto createBooking(Long userId, BookingDto bookingDto) {
        ResponseEntity<Object> response = baseClient.post(API_PREFIX, userId, null, bookingDto);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(response.getBody(), BookingResponseDto.class);
    }

    public BookingResponseDto updateBookingStatus(Long bookingId, Long ownerId, boolean approved) {
        String url = String.format("%s/%d?approved=%b", API_PREFIX, bookingId, approved);
        ResponseEntity<Object> response = baseClient.patch(url, ownerId, null, null);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(response.getBody(), BookingResponseDto.class);
    }


    public BookingResponseDto getBooking(Long bookingId, Long userId) {
        String url = String.format("%s/%d", API_PREFIX, bookingId);
        ResponseEntity<Object> response = baseClient.get(url, userId, null);
        if (response.getBody() instanceof BookingResponseDto) {
            return (BookingResponseDto) response.getBody();
        }
        return null;
    }


    public List<BookingResponseDto> getUserBookings(Long userId, String state, int from, int size) {
        String url = String.format("%s?state=%s&from=%d&size=%d", API_PREFIX, state, from, size);
        return baseClient.getList(url, userId);
    }

    public List<BookingResponseDto> getOwnerBookings(Long ownerId, String state, int from, int size) {
        String url = String.format("%s/owner?state=%s&from=%d&size=%d", API_PREFIX, state, from, size);
        return baseClient.getList(url, ownerId);
    }
}

