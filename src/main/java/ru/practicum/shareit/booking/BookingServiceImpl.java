package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    public BookingDto addBooking(Long userId, BookingDto bookingDto) {
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

        return toDto(bookingRepository.save(booking));
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

    private BookingDto toDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getItem().getId(),
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
}

