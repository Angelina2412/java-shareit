package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.BookerDto;
import ru.practicum.shareit.item.dto.ItemDto;
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
        Item item = itemService.getItemEntityById(bookingDto.getItemId());

        if (item == null) {
            throw new IllegalArgumentException("Item с таким id не найден");
        }

        // Проверка: дата начала не может быть в прошлом
        if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Дата начала бронирования не может быть в прошлом");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // Проверка: дата начала не может быть равна дате окончания
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
        // Проверка: BookingId не может быть null
        if (bookingId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "ID бронирования не может быть null");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // Находим бронирование по ID
        Booking booking = bookingRepository.findById(bookingId)
                                           .orElseThrow(() -> new NotFoundException("Booking not found"));

        // Проверка на права владельца. Статус может изменить только владелец предмета
        if (!booking.getItem().getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("You are not authorized to change the status of this booking");
        }

        // Проверка: статус может быть изменен только на APPROVED или REJECTED
        if (approved != true && approved != false) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Статус бронирования должен быть APPROVED или REJECTED");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // Обновляем статус бронирования
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);

        // Возвращаем ответ с полным объектом item
        return ResponseEntity.ok(toResponse(updatedBooking));
    }


    @Override
    public ResponseEntity<Map<String, Object>> getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                                           .orElseThrow(() -> new NotFoundException("Booking not found"));

        return ResponseEntity.ok(toResponse(booking));
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> getUserBookings(Long userId, String state) {
        List<Booking> bookings = filterBookingsByState(bookingRepository.findAllByBookerId(userId), state);

        List<Map<String, Object>> responseList = bookings.stream()
                                                         .map(this::toResponse)
                                                         .toList();
        return ResponseEntity.ok(responseList);
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> getOwnerBookings(Long ownerId, String state) {
        try {
            // Получаем бронирования владельца
            List<Booking> bookings = filterBookingsByState(bookingRepository.findAllByItemOwnerId(ownerId), state);
            if (bookings.isEmpty()) {
                throw new NotFoundException("No bookings found for owner with id " + ownerId);
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



    public BookingDto toDto(Booking booking) {
        Item item = itemService.getItemEntityById(booking.getItem().getId()); // Берем Item по itemId

        return new BookingDto(
                booking.getId(),
                booking.getItem().getId(), // Оставляем itemId
                new BookerDto(booking.getBooker().getId()),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus()
        );
    }

    private List<Booking> filterBookingsByState(List<Booking> bookings, String state) {
        LocalDateTime now = LocalDateTime.now();
        return switch (state) {
            case "CURRENT" -> bookings.stream()
                                      .filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now))
                                      .toList();
            case "PAST" -> bookings.stream()
                                   .filter(b -> b.getEnd().isBefore(now))
                                   .toList();
            case "FUTURE" -> bookings.stream()
                                     .filter(b -> b.getStart().isAfter(now))
                                     .toList();
            case "WAITING" -> bookings.stream()
                                      .filter(b -> b.getStatus() == BookingStatus.WAITING)
                                      .toList();
            case "REJECTED" -> bookings.stream()
                                       .filter(b -> b.getStatus() == BookingStatus.REJECTED)
                                       .toList();
            default -> bookings; // "ALL"
        };
    }

    public Map<String, Object> toResponse(Booking booking) {
        Item item = itemService.getItemEntityById(booking.getItem().getId()); // Загружаем item

        Map<String, Object> response = new HashMap<>();
        response.put("id", booking.getId());
        response.put("start", booking.getStart());
        response.put("end", booking.getEnd());
        response.put("status", booking.getStatus());

        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("id", item.getId());
        itemMap.put("name", item.getName());

        response.put("item", itemMap);

        Map<String, Object> bookerMap = new HashMap<>();
        bookerMap.put("id", booking.getBooker().getId());

        response.put("booker", bookerMap);

        return response;
    }
}

