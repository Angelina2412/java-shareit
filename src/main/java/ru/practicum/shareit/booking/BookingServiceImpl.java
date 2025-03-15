package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    public ResponseEntity<Map<String, Object>> addBooking(Long userId, BookingDto bookingDto) {
        if (bookingDto.getItemId() == null) {
            throw new IllegalArgumentException("ID объекта Item не может быть null");
        }

        User booker = userService.getUserById(userId);
        Item item = itemService.getItemEntityById(bookingDto.getItemId(), userId);

        if (item == null) {
            throw new IllegalArgumentException("Item с таким id не найден");
        }

        if (!item.isAvailable()) {
            throw new IllegalStateException("Этот предмет сейчас недоступен для бронирования");
        }

        if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Дата начала бронирования не может быть в прошлом");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        if (bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Дата начала бронирования не может быть равна дате окончания");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);

        return ResponseEntity.ok(toResponse(savedBooking));
    }

    @Override
    public ResponseEntity<Map<String, Object>> updateBookingStatus(Long bookingId, Long ownerId, boolean approved)
            throws AccessDeniedException {
        Booking booking = bookingRepository.findById(bookingId)
                                           .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Нет доступа поменять статус бронирования");
        }

        BookingStatus status;
        if (approved) {
            status = BookingStatus.APPROVED;
        } else {
            status = BookingStatus.REJECTED;
        }
        booking.setStatus(status);
        return ResponseEntity.ok(toResponse(bookingRepository.save(booking)));
    }


    @Override
    public ResponseEntity<Map<String, Object>> getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                                           .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
        return ResponseEntity.ok(toResponse(booking));
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> getUserBookings(Long userId, String state) {
        return ResponseEntity.ok(
                filterBookingsByState(bookingRepository.findAllByBookerId(userId), state).stream()
                                                                                         .map(this::toResponse)
                                                                                         .toList()
        );
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> getOwnerBookings(Long ownerId, String state) {
        try {
            List<Booking> bookings = filterBookingsByState(bookingRepository.findAllByItemOwnerId(ownerId), state);
            if (bookings.isEmpty()) {
                throw new NotFoundException("Нет бронирований для пользователя с id " + ownerId);
            }

            List<Map<String, Object>> responseList = bookings.stream()
                                                             .map(this::toResponse)
                                                             .toList();
            return ResponseEntity.ok(responseList);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonList(Map.of("error", e.getMessage())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonList(Map.of("error", e.getMessage())));
        }
    }

    private List<Booking> filterBookingsByState(List<Booking> bookings, String state) {
        LocalDateTime now = LocalDateTime.now();
        return switch (state) {
            case "CURRENT" -> bookings.stream().filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now)).toList();
            case "PAST" -> bookings.stream().filter(b -> b.getEnd().isBefore(now)).toList();
            case "FUTURE" -> bookings.stream().filter(b -> b.getStart().isAfter(now)).toList();
            case "WAITING" -> bookings.stream().filter(b -> b.getStatus() == BookingStatus.WAITING).toList();
            case "REJECTED" -> bookings.stream().filter(b -> b.getStatus() == BookingStatus.REJECTED).toList();
            default -> bookings;
        };
    }

    private Map<String, Object> toResponse(Booking booking) {
        return Map.of(
                "id", booking.getId(),
                "start", booking.getStart(),
                "end", booking.getEnd(),
                "status", booking.getStatus(),
                "item", Map.of(
                        "id", booking.getItem().getId(),
                        "name", booking.getItem().getName()
                ),
                "booker", Map.of(
                        "id", booking.getBooker().getId()
                )
        );
    }
}

