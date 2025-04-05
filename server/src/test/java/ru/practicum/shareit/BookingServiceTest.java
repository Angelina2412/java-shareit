package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.BookerDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@DataJpaTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@ComponentScan(basePackages = "ru.practicum.shareit")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceTest {
    @Autowired
    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;
    private User owner;
    private User user;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = new User(null, "owner", "owner@google.com");
        userRepository.save(owner);

        user = new User(null, "user", "user@google.com");
        userRepository.save(user);

        item = new Item("Drill", "Power drill", true, owner);
        itemRepository.save(item);
    }

    @Test
    void getOwnerBookings_ShouldReturnCurrentBookings_WhenStateIsCurrent() {
        LocalDateTime now = LocalDateTime.now();
        Booking currentBooking = new Booking(item, user, now.minusDays(1), now.plusDays(1), BookingStatus.WAITING);
        bookingRepository.save(currentBooking);

        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(owner.getId(), "CURRENT");

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
    }


    @Test
    void getOwnerBookings_ShouldReturtnPastBookings_WhenStateIsPast() {
        LocalDateTime now = LocalDateTime.now();
        Booking pastBooking = new Booking(item, user, now.minusDays(3), now.minusDays(1), BookingStatus.REJECTED);
        bookingRepository.save(pastBooking);

        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(owner.getId(), "PAST");

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void getOwnerBookings_ShouldReturnFutureBookings_WhenStateIsFuture() {
        LocalDateTime now = LocalDateTime.now();
        Booking futureBooking = new Booking(item, user, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING);
        bookingRepository.save(futureBooking);

        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(owner.getId(), "FUTURE");

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void getOwnerBookings_ShouldReturnWaitingBookings_WhenStateIsWaiting() {
        LocalDateTime now = LocalDateTime.now();
        Booking waitingBooking = new Booking(item, user, now.minusDays(1), now.plusDays(1), BookingStatus.WAITING);
        bookingRepository.save(waitingBooking);

        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(owner.getId(), "WAITING");

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void getOwnerBookings_ShouldReturnRejectedBookings_WhenStateIsRejected() {
        LocalDateTime now = LocalDateTime.now();
        Booking rejectedBooking = new Booking(item, user, now.minusDays(3), now.minusDays(1), BookingStatus.REJECTED);
        bookingRepository.save(rejectedBooking);

        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(owner.getId(), "REJECTED");

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void getOwnerBookings_ShouldReturnAllBookings_WhenStateIsUnknown() {
        LocalDateTime now = LocalDateTime.now();
        Booking pastBooking = new Booking(item, user, now.minusDays(3), now.minusDays(1), BookingStatus.REJECTED);
        Booking futureBooking = new Booking(item, user, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING);
        Booking currentBooking = new Booking(item, user, now.minusDays(1), now.plusDays(1), BookingStatus.WAITING);

        bookingRepository.save(pastBooking);
        bookingRepository.save(futureBooking);
        bookingRepository.save(currentBooking);

        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(owner.getId(), "UNKNOWN");

        assertThat(bookings).hasSize(3);
    }

    @Test
    void getOwnerBookings_ShouldThrowNotFoundException_WhenOwnerDoesNotExist() {
        assertThrows(NotFoundException.class, () -> bookingService.getOwnerBookings(999L, "CURRENT"));
    }

    @Test
    void createBooking_ShouldThrowException_WhenStartDateIsInThePast() {
        User user = new User(null, "test_user", "user@example.com");
        userRepository.save(user);

        Item item = new Item(null, "Test Item", "Description", true, user, null);
        itemRepository.save(item);

        BookingDto bookingDto = new BookingDto(
                null,
                item.getId(),
                new BookerDto(user.getId()),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(2),
                null
        );

        assertThrows(IllegalArgumentException.class, () -> bookingService.addBooking(user.getId(), bookingDto));
    }

    @Test
    void createBooking_ShouldThrowException_WhenItemIsNotAvailable() {
        User user = new User(null, "test_user", "user@example.com");
        userRepository.save(user);

        Item item = new Item(null, "Test Item", "Description", false, user, null);
        itemRepository.save(item);

        BookingDto bookingDto = new BookingDto(
                null,
                item.getId(),
                new BookerDto(user.getId()),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                null
        );

        assertThrows(IllegalStateException.class, () -> bookingService.addBooking(user.getId(), bookingDto));
    }

    @Test
    void createBooking_ShouldThrowException_WhenItemIdIsNull() {
        User user = new User(null, "test_user", "user@example.com");
        userRepository.save(user);

        BookingDto bookingDto = new BookingDto(
                null, null, new BookerDto(user.getId()),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), null
        );

        assertThrows(IllegalArgumentException.class, () -> bookingService.addBooking(user.getId(), bookingDto));
    }

    @Test
    void updateBookingStatus_ShouldThrowException_WhenNotOwner() {
        User owner = new User(null, "owner", "owner@example.com");
        userRepository.save(owner);

        User booker = new User(null, "booker", "booker@example.com");
        userRepository.save(booker);

        User anotherUser = new User(null, "another", "another@example.com");
        userRepository.save(anotherUser);

        Item item = new Item(null, "Test Item", "Description", true, owner, null);
        itemRepository.save(item);

        Booking booking = new Booking(null, item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING);
        bookingRepository.save(booking);

        assertThrows(ForbiddenException.class, () -> bookingService.updateBookingStatus(booking.getId(), anotherUser.getId(), true));
    }

}
