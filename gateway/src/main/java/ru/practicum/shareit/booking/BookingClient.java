package ru.practicum.shareit.booking;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingClient {

    private static final String API_PREFIX = "/bookings";
    private final BaseClient baseClient;
    private final ObjectMapper objectMapper;

    public BookingResponseDto createBooking(Long userId, BookingDto bookingDto) {
        ResponseEntity<Object> response = baseClient.post(API_PREFIX, userId, null, bookingDto);
        return processResponse(response, "Ошибка бронирования");
    }

    public BookingResponseDto updateBookingStatus(Long bookingId, Long ownerId, boolean approved) {
        String url = String.format("%s/%d?approved=%b", API_PREFIX, bookingId, approved);
        ResponseEntity<Object> response = baseClient.patch(url, ownerId, null, null);
        return processResponse(response, "Ошибка обновления статуса бронирования");
    }

    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        String url = String.format("%s/%d", API_PREFIX, bookingId);
        ResponseEntity<Object> response = baseClient.get(url, userId, null);
        return processResponse(response, "Ошибка получения бронирования");
    }

    public List<BookingResponseDto> getUserBookings(Long userId, String state, int from, int size) {
        String url = String.format("%s?state=%s&from=%d&size=%d", API_PREFIX, state, from, size);
        return baseClient.getList(url, userId);
    }


    public List<BookingResponseDto> getOwnerBookings(Long ownerId, String state, int from, int size) {
        String url = String.format("%s/owner?state=%s&from=%d&size=%d", API_PREFIX, state, from, size);

        ResponseEntity<Object> response;
        try {
            response = baseClient.get(url, ownerId);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException(String.format("Ошибка при получении бронирований: %s", e.getMessage()), e);
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException(String.format("Ошибка сервиса: %s", response.getStatusCode()));
        }

        if (response.getBody() instanceof List<?>) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(response.getBody(), new TypeReference<List<BookingResponseDto>>() {
            });
        }

        throw new RuntimeException("Некорректный ответ от сервиса: ожидался список бронирований.");
    }


    private BookingResponseDto processResponse(ResponseEntity<Object> response, String errorMessage) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> errorResponse = objectMapper.convertValue(response.getBody(), Map.class);

            String error = errorResponse != null ? (String) errorResponse.get("message") : "Unknown error";
            String details = errorResponse != null ? (String) errorResponse.get("details") : "No details available";

            if ("Not found".equalsIgnoreCase(error)) {
                throw new NotFoundException(details);
            } else {
                throw new RuntimeException(errorMessage + ": " + error + " - " + details);
            }
        }
        return objectMapper.convertValue(response.getBody(), BookingResponseDto.class);
    }


}


