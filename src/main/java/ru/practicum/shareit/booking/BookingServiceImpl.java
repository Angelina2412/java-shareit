package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
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

import java.time.LocalDateTime;
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
    public BookingDto updateBookingStatus(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                                           .orElseThrow(() -> new NotFoundException("Booking not found"));

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                                           .orElseThrow(() -> new NotFoundException("Booking not found"));

        return toDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, String state) {
        List<Booking> bookings = filterBookingsByState(bookingRepository.findAllByBookerId(userId), state);
        return bookings.stream().map(this::toDto).toList();
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long ownerId, String state) {
        List<Booking> bookings = filterBookingsByState(bookingRepository.findAllByItemOwnerId(ownerId), state);
        return bookings.stream().map(this::toDto).toList();
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

