package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    public BookingResponseDto addBooking(Long userId, BookingDto bookingDto) {
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
            throw new IllegalArgumentException("Дата начала бронирования не может быть в прошлом");
        }

        if (bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new IllegalArgumentException("Дата начала бронирования не может быть равна дате окончания");
        }

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        return toResponse(savedBooking);
    }

    @Override
    public BookingResponseDto updateBookingStatus(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Нет доступа поменять статус бронирования");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return toResponse(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
        return toResponse(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, String state) {
        List<Booking> bookings = filterBookingsByState(bookingRepository.findAllByBookerId(userId), state);
        return bookings.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, String state) {
        List<Booking> bookings = filterBookingsByState(bookingRepository.findAllByItemOwnerId(ownerId), state);
        return bookings.stream().map(this::toResponse).collect(Collectors.toList());
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

    private BookingResponseDto toResponse(Booking booking) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                booking.getItem().getId(),
                booking.getItem().getName(),
                booking.getBooker().getId()
        );
    }
}

