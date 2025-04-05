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
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.BookerDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@ComponentScan(basePackages = "ru.practicum.shareit")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceNewTest {
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
    void createBooking_ShouldSaveBooking() {
        User user = new User(null, "test_user", "user@example.com");
        userRepository.save(user);

        Item item = new Item(null, "Test Item", "Description", true, user, null);
        itemRepository.save(item);

        BookingDto bookingDto = new BookingDto(
                null,
                item.getId(),
                new BookerDto(user.getId()),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                null
        );

        BookingResponseDto savedBooking = bookingService.addBooking(user.getId(), bookingDto);

        Booking booking = bookingRepository.findById(savedBooking.getId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        assertThat(booking, is(notNullValue()));
        assertThat(booking.getItem().getId(), is(item.getId()));
        assertThat(booking.getBooker().getId(), is(user.getId()));
        assertThat(booking.getStart(), is(bookingDto.getStart()));
        assertThat(booking.getEnd(), is(bookingDto.getEnd()));
    }

    @Test
    void updateBookingStatus_ShouldApproveBooking_WhenOwnerUpdates() {
        User owner = new User(null, "owner", "owner@example.com");
        userRepository.save(owner);

        User booker = new User(null, "booker", "booker@example.com");
        userRepository.save(booker);

        Item item = new Item(null, "Test Item", "Description", true, owner, null);
        itemRepository.save(item);

        Booking booking = new Booking(null, item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING);
        bookingRepository.save(booking);

        BookingResponseDto updated = bookingService.updateBookingStatus(booking.getId(), owner.getId(), true);

        assertThat(updated.getStatus(), is(BookingStatus.APPROVED));
    }

    @Test
    void getBookingById_ShouldReturnCorrectBooking() throws AccessDeniedException {
        User owner = new User(null, "owner", "owner@example.com");
        userRepository.save(owner);

        User booker = new User(null, "booker", "booker@example.com");
        userRepository.save(booker);

        Item item = new Item(null, "Test Item", "Description", true, owner, null);
        itemRepository.save(item);

        Booking booking = new Booking(null, item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING);
        bookingRepository.save(booking);

        BookingResponseDto found = bookingService.getBookingById(booking.getId(), booker.getId());

        assertThat(found.getId(), is(booking.getId()));
        assertThat(found.getStatus(), is(booking.getStatus()));
    }

    @Test
    void getUserBookings_ShouldFilterByStatus() {
        User booker = new User(null, "booker", "booker@example.com");
        userRepository.save(booker);

        User owner = new User(null, "owner", "owner@example.com");
        userRepository.save(owner);

        Item item = new Item(null, "Test Item", "Description", true, owner, null);
        itemRepository.save(item);

        Booking pastBooking = new Booking(null, item, booker, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1), BookingStatus.APPROVED);
        bookingRepository.save(pastBooking);

        Booking futureBooking = new Booking(null, item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING);
        bookingRepository.save(futureBooking);

        List<BookingResponseDto> pastBookings = bookingService.getUserBookings(booker.getId(), "PAST");
        assertThat(pastBookings.size(), is(1));
        assertThat(pastBookings.get(0).getId(), is(pastBooking.getId()));

        List<BookingResponseDto> futureBookings = bookingService.getUserBookings(booker.getId(), "FUTURE");
        assertThat(futureBookings.size(), is(1));
        assertThat(futureBookings.get(0).getId(), is(futureBooking.getId()));
    }

    @Test
    void addBooking_ShouldThrowNotFound_WhenItemIsNull() {
        User user = new User(null, "test_user", "user@example.com");
        userRepository.save(user);

        BookingDto bookingDto = new BookingDto(
                null,
                999L,
                new BookerDto(user.getId()),
                LocalDateTime.now().plusDays(1).withNano(0),
                LocalDateTime.now().plusDays(2).withNano(0),
                null
        );

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(user.getId(), bookingDto));

        assertThat(exception.getMessage(), containsString("Item с ID 999 не найден."));
    }

    @Test
    void addBooking_ShouldThrowIllegalArgument_WhenStartEqualsEnd() {
        User user = new User(null, "test_user", "user@example.com");
        userRepository.save(user);

        Item item = new Item(null, "Test Item", "Description", true, user, null);
        itemRepository.save(item);

        LocalDateTime date = LocalDateTime.now().plusDays(1).withNano(0);

        BookingDto bookingDto = new BookingDto(
                null,
                item.getId(),
                new BookerDto(user.getId()),
                date,
                date,
                null
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.addBooking(user.getId(), bookingDto));

        assertThat(exception.getMessage(), containsString("Дата начала бронирования не может быть равна дате окончания"));
    }

    @Test
    void addBooking_ShouldThrowNotFound_WhenItemDoesNotExist() {
        User user = new User(null, "test_user", "user@example.com");
        userRepository.save(user);

        Long nonexistentItemId = 9999L;

        BookingDto bookingDto = new BookingDto(
                null,
                nonexistentItemId,
                new BookerDto(user.getId()),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                null
        );

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(user.getId(), bookingDto));

        assertThat(exception.getMessage(), containsString("Item с ID " + nonexistentItemId + " не найден"));
    }

    @Test
    void testBookingDtoConstructor() {
        Long expectedId = 1L;
        Long expectedBookerId = 2L;

        BookingDto bookingDto = new BookingDto(expectedId, expectedBookerId);

        assertEquals(expectedId, bookingDto.getId());
        assertEquals(expectedBookerId, bookingDto.getBooker().getId());
    }
}


