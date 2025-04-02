package ru.practicum.shareit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
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
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.BookerDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@ComponentScan(basePackages = "ru.practicum.shareit")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceTest {
    private final EntityManager em;
    @Autowired
    private final BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Test
    void createBooking_ShouldSaveBooking() {
        User user = new User(null, "test_user", "user@example.com");
        em.persist(user);

        Item item = new Item(null, "Test Item", "Description", true, user, null);
        em.persist(item);

        BookingDto bookingDto = new BookingDto(
                null,
                item.getId(),
                new BookerDto(user.getId()),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                null
        );

        BookingResponseDto savedBooking = bookingService.addBooking(user.getId(), bookingDto);

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking booking = query.setParameter("id", savedBooking.getId()).getSingleResult();

        assertThat(booking, is(notNullValue()));
        assertThat(booking.getItem().getId(), is(item.getId()));
        assertThat(booking.getBooker().getId(), is(user.getId()));
        assertThat(booking.getStart(), is(bookingDto.getStart()));
        assertThat(booking.getEnd(), is(bookingDto.getEnd()));
    }

    @Test
    void createBooking_ShouldThrowException_WhenStartDateIsInThePast() {
        User user = new User(null, "test_user", "user@example.com");
        em.persist(user);

        Item item = new Item(null, "Test Item", "Description", true, user, null);
        em.persist(item);

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
        em.persist(user);

        Item item = new Item(null, "Test Item", "Description", false, user, null);
        em.persist(item);

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
        em.persist(user);

        BookingDto bookingDto = new BookingDto(
                null, null, new BookerDto(user.getId()),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), null
        );

        assertThrows(IllegalArgumentException.class, () -> bookingService.addBooking(user.getId(), bookingDto));
    }

    @Test
    void updateBookingStatus_ShouldApproveBooking_WhenOwnerUpdates() {
        User owner = new User(null, "owner", "owner@example.com");
        em.persist(owner);

        User booker = new User(null, "booker", "booker@example.com");
        em.persist(booker);

        Item item = new Item(null, "Test Item", "Description", true, owner, null);
        em.persist(item);

        Booking booking = new Booking(null, item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING);
        em.persist(booking);

        BookingResponseDto updated = bookingService.updateBookingStatus(booking.getId(), owner.getId(), true);

        assertThat(updated.getStatus(), is(BookingStatus.APPROVED));
    }

    @Test
    void updateBookingStatus_ShouldThrowException_WhenNotOwner() {
        User owner = new User(null, "owner", "owner@example.com");
        em.persist(owner);

        User booker = new User(null, "booker", "booker@example.com");
        em.persist(booker);

        User anotherUser = new User(null, "another", "another@example.com");
        em.persist(anotherUser);

        Item item = new Item(null, "Test Item", "Description", true, owner, null);
        em.persist(item);

        Booking booking = new Booking(null, item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING);
        em.persist(booking);

        assertThrows(ForbiddenException.class, () -> bookingService.updateBookingStatus(booking.getId(), anotherUser.getId(), true));
    }

    @Test
    void getBookingById_ShouldReturnCorrectBooking() throws AccessDeniedException {
        User owner = new User(null, "owner", "owner@example.com");
        em.persist(owner);

        User booker = new User(null, "booker", "booker@example.com");
        em.persist(booker);

        Item item = new Item(null, "Test Item", "Description", true, owner, null);
        em.persist(item);

        Booking booking = new Booking(null, item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING);
        em.persist(booking);

        BookingResponseDto found = bookingService.getBookingById(booking.getId(), booker.getId());

        assertThat(found.getId(), is(booking.getId()));
        assertThat(found.getStatus(), is(booking.getStatus()));
    }

    @Test
    void getUserBookings_ShouldFilterByStatus() {
        User booker = new User(null, "booker", "booker@example.com");
        em.persist(booker);

        User owner = new User(null, "owner", "owner@example.com");
        em.persist(owner);

        Item item = new Item(null, "Test Item", "Description", true, owner, null);
        em.persist(item);

        Booking pastBooking = new Booking(null, item, booker, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1), BookingStatus.APPROVED);
        em.persist(pastBooking);

        Booking futureBooking = new Booking(null, item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING);
        em.persist(futureBooking);

        List<BookingResponseDto> pastBookings = bookingService.getUserBookings(booker.getId(), "PAST");
        assertThat(pastBookings.size(), is(1));
        assertThat(pastBookings.get(0).getId(), is(pastBooking.getId()));

        List<BookingResponseDto> futureBookings = bookingService.getUserBookings(booker.getId(), "FUTURE");
        assertThat(futureBookings.size(), is(1));
        assertThat(futureBookings.get(0).getId(), is(futureBooking.getId()));
    }

}
