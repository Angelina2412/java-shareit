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
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.CommentDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@ComponentScan(basePackages = "ru.practicum.shareit")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceTest {
    private final EntityManager em;
    private final ItemService itemService;
    private final UserRepository userRepository;

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;


    @Test
    void addItem_ShouldSaveItem() throws BadRequestException {
        User user = new User(null, "test_user", "user@example.com");
        em.persist(user);

        ItemDto itemDto = new ItemDto(null, "Test Item", "Description", true, null);
        ItemDto savedItem = itemService.addItem(user.getId(), itemDto);

        TypedQuery<Item> query = em.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item item = query.setParameter("id", savedItem.getId()).getSingleResult();

        assertThat(item, is(notNullValue()));
        assertThat(item.getName(), is(itemDto.getName()));
        assertThat(item.getDescription(), is(itemDto.getDescription()));
        assertThat(item.isAvailable(), is(itemDto.getAvailable()));
    }

    @Test
    void addItem_ShouldThrowException_WhenUserNotFound() {
        ItemDto itemDto = new ItemDto(null, "Test Item", "Description", true, null);
        assertThrows(NotFoundException.class, () -> itemService.addItem(999L, itemDto));
    }

    @Test
    void updateItem_ShouldUpdateItem() throws NotFoundException, ForbiddenException {
        User user = new User(null, "test_user", "user@example.com");
        em.persist(user);

        Item item = new Item("Old Name", "Old Description", true, user);
        em.persist(item);

        ItemDto itemDto = new ItemDto(item.getId(), "New Name", "New Description", false, null);
        ItemDto updatedItem = itemService.updateItem(user.getId(), item.getId(), itemDto);

        assertThat(updatedItem.getName(), is("New Name"));
        assertThat(updatedItem.getDescription(), is("New Description"));
        assertThat(updatedItem.getAvailable(), is(false));
    }

    @Test
    void updateItem_ShouldThrowException_WhenItemNotFound() {
        User user = new User(null, "test_user", "user@example.com");
        em.persist(user);

        ItemDto itemDto = new ItemDto(999L, "New Name", "New Description", true, null);
        assertThrows(NotFoundException.class, () -> itemService.updateItem(user.getId(), 999L, itemDto));
    }

    @Test
    void updateItem_ShouldThrowException_WhenNotOwner() {
        User user1 = new User(null, "test_user_1", "user1@example.com");
        User user2 = new User(null, "test_user_2", "user2@example.com");
        em.persist(user1);
        em.persist(user2);

        Item item = new Item("Item Name", "Item Description", true, user1);
        em.persist(item);

        ItemDto itemDto = new ItemDto(item.getId(), "New Name", "New Description", true, null);
        assertThrows(ForbiddenException.class, () -> itemService.updateItem(user2.getId(), item.getId(), itemDto));
    }

    @Test
    void getItemById_ShouldReturnItem() throws NotFoundException {
        User user = new User(null, "test_user", "user@example.com");
        em.persist(user);

        Item item = new Item("Item Name", "Item Description", true, user);
        em.persist(item);

        ItemDto itemDto = itemService.getItemById(item.getId(), user.getId());

        assertThat(itemDto.getId(), is(item.getId()));
        assertThat(itemDto.getName(), is(item.getName()));
        assertThat(itemDto.getDescription(), is(item.getDescription()));
        assertThat(itemDto.getAvailable(), is(item.isAvailable()));
    }

    @Test
    void getItemById_ShouldThrowException_WhenItemNotFound() {
        assertThrows(NotFoundException.class, () -> itemService.getItemById(999L, 1L));
    }

    @Test
    void getAllItemsByOwner_ShouldReturnItems() {
        User user = new User(null, "test_user", "user@example.com");
        em.persist(user);

        Item item1 = new Item("Item 1", "Description", true, user);
        Item item2 = new Item("Item 2", "Description", true, user);
        em.persist(item1);
        em.persist(item2);

        List<ItemDto> items = itemService.getAllItemsByOwner(user.getId());

        assertThat(items.size(), is(2));
        assertThat(items.get(0).getName(), is("Item 1"));
        assertThat(items.get(1).getName(), is("Item 2"));
    }

    @Test
    void searchItems_ShouldReturnMatchingItems() {
        User user = new User(null, "test_user", "user@example.com");
        em.persist(user);

        Item item1 = new Item("Item 1", "Description", true, user);
        Item item2 = new Item("Test Item", "Description", true, user);
        em.persist(item1);
        em.persist(item2);

        List<ItemDto> items = itemService.searchItems("Test");

        assertThat(items.size(), is(1));
        assertThat(items.get(0).getName(), is("Test Item"));
    }

    @Test
    void searchItems_ShouldReturnEmptyList_WhenNoMatch() {
        User user = new User(null, "test_user", "user@example.com");
        em.persist(user);

        Item item = new Item("Item Name", "Description", true, user);
        em.persist(item);

        List<ItemDto> items = itemService.searchItems("Nonexistent");

        assertThat(items.size(), is(0));
    }

    @Test
    void addComment_ShouldThrowException_WhenItemNotFound() {
        CommentDto commentDto = new CommentDto("Great item!");
        assertThrows(NotFoundException.class, () -> itemService.addComment(999L, 1L, commentDto));
    }

    @Test
    void addComment_ShouldThrowException_WhenUserHasNotBookedItem() {
        User user = new User(null, "test_user", "user@example.com");
        em.persist(user);

        Item item = new Item("Item Name", "Item Description", true, user);
        em.persist(item);

        CommentDto commentDto = new CommentDto("Great item!");
        assertThrows(BadRequestException.class, () -> itemService.addComment(item.getId(), user.getId(), commentDto));
    }

    @Test
    void getCommentsByItemId_ShouldReturnComments() {
        User user = new User(null, "test_user", "user@example.com");
        em.persist(user);

        Item item = new Item("Item Name", "Item Description", true, user);
        em.persist(item);

        Comment comment = new Comment(item, user, "Great item!");
        em.persist(comment);

        List<CommentDto> comments = itemService.getCommentsByItemId(item.getId());

        assertThat(comments.size(), is(1));
        assertThat(comments.get(0).getText(), is("Great item!"));
    }

    @Test
    void addItem_ShouldThrowException_WhenNameIsEmpty() {
        User user = new User(null, "test_user", "user@example.com");
        em.persist(user);

        ItemDto itemDto = new ItemDto(null, "", "Description", true, null);

        assertThrows(BadRequestException.class, () -> itemService.addItem(user.getId(), itemDto));
    }

    @Test
    void updateItem_ShouldThrowException_WhenOwnerNotFound() {
        ItemDto itemDto = new ItemDto(1L, "New Name", "New Description", true, null);

        assertThrows(NotFoundException.class, () -> itemService.updateItem(999L, 1L, itemDto));
    }

    @Test
    void addComment_ShouldThrowException_WhenCommentTextIsEmpty() {
        User user = new User(null, "test_user", "user@example.com");
        em.persist(user);

        Item item = new Item("Item Name", "Item Description", true, user);
        em.persist(item);

        CommentDto commentDto = new CommentDto("");

        assertThrows(BadRequestException.class, () -> itemService.addComment(item.getId(), user.getId(), commentDto));
    }

    @Test
    void updateItem_ShouldUpdateOnlyName_WhenOnlyNameProvided() throws NotFoundException, ForbiddenException {
        User user = new User(null, "test_user", "user@example.com");
        em.persist(user);

        Item item = new Item("Old Name", "Old Description", true, user);
        em.persist(item);

        ItemDto itemDto = new ItemDto(item.getId(), "New Name", null, null, null);
        ItemDto updatedItem = itemService.updateItem(user.getId(), item.getId(), itemDto);

        assertThat(updatedItem.getName(), is("New Name"));
        assertThat(updatedItem.getDescription(), is("Old Description")); // Должно остаться неизменным
        assertThat(updatedItem.getAvailable(), is(true)); // Должно остаться неизменным
    }

    @Test
    void updateItem_ShouldUpdateOnlyDescription_WhenOnlyDescriptionProvided() throws NotFoundException, ForbiddenException {
        User user = new User(null, "test_user", "user@example.com");
        em.persist(user);

        Item item = new Item("Old Name", "Old Description", true, user);
        em.persist(item);

        ItemDto itemDto = new ItemDto(item.getId(), null, "New Description", null, null);
        ItemDto updatedItem = itemService.updateItem(user.getId(), item.getId(), itemDto);

        assertThat(updatedItem.getName(), is("Old Name")); // Должно остаться неизменным
        assertThat(updatedItem.getDescription(), is("New Description"));
        assertThat(updatedItem.getAvailable(), is(true)); // Должно остаться неизменным
    }

    @Test
    void updateItem_ShouldUpdateOnlyAvailability_WhenOnlyAvailabilityProvided() throws NotFoundException, ForbiddenException {
        User user = new User(null, "test_user", "user@example.com");
        em.persist(user);

        Item item = new Item("Old Name", "Old Description", true, user);
        em.persist(item);

        ItemDto itemDto = new ItemDto(item.getId(), null, null, false, null);
        ItemDto updatedItem = itemService.updateItem(user.getId(), item.getId(), itemDto);

        assertThat(updatedItem.getName(), is("Old Name")); // Должно остаться неизменным
        assertThat(updatedItem.getDescription(), is("Old Description")); // Должно остаться неизменным
        assertThat(updatedItem.getAvailable(), is(false)); // Должно измениться
    }

    @Test
    void updateItem_ShouldNotChangeAnything_WhenAllFieldsAreNull() throws NotFoundException, ForbiddenException {
        User user = new User(null, "test_user", "user@example.com");
        em.persist(user);

        Item item = new Item("Old Name", "Old Description", true, user);
        em.persist(item);

        ItemDto itemDto = new ItemDto(item.getId(), null, null, null, null);
        ItemDto updatedItem = itemService.updateItem(user.getId(), item.getId(), itemDto);

        assertThat(updatedItem.getName(), is("Old Name"));
        assertThat(updatedItem.getDescription(), is("Old Description"));
        assertThat(updatedItem.getAvailable(), is(true));
    }

    @Test
    void updateItem_ShouldUpdateWithEmptyStrings() throws NotFoundException, ForbiddenException {
        User user = new User(null, "test_user", "user@example.com");
        em.persist(user);

        Item item = new Item("Old Name", "Old Description", true, user);
        em.persist(item);

        ItemDto itemDto = new ItemDto(item.getId(), "", "", null, null);
        ItemDto updatedItem = itemService.updateItem(user.getId(), item.getId(), itemDto);

        assertThat(updatedItem.getName(), is(""));
        assertThat(updatedItem.getDescription(), is(""));
    }

    @Test
    void updateItem_ShouldNotSave_WhenNoChangesMade() throws NotFoundException, ForbiddenException {
        User user = new User(null, "test_user", "user@example.com");
        em.persist(user);

        Item item = new Item("Old Name", "Old Description", true, user);
        em.persist(item);

        ItemDto itemDto = new ItemDto(item.getId(), "Old Name", "Old Description", true, null);

        ItemDto updatedItem = itemService.updateItem(user.getId(), item.getId(), itemDto);

        assertThat(updatedItem.getName(), is("Old Name"));
        assertThat(updatedItem.getDescription(), is("Old Description"));
        assertThat(updatedItem.getAvailable(), is(true));
    }

    @Test
    void searchItems_ShouldReturnEmptyList_WhenTextIsNull() {
        List<ItemDto> items = itemService.searchItems(null);

        assertThat(items, is(notNullValue()));
        assertThat(items.size(), is(0));
    }

    @Test
    void searchItems_ShouldReturnEmptyList_WhenTextIsBlank() {
        List<ItemDto> items = itemService.searchItems("   ");

        assertThat(items, is(notNullValue()));
        assertThat(items.size(), is(0));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenAvailableIsNull() {
        User owner = userRepository.save(new User(null, "First", "john@google.com"));
        Long ownerId = owner.getId();

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Test item");
        itemDto.setDescription("Test description");
        itemDto.setAvailable(null);
        itemDto.setRequestId(null);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                itemService.addItem(ownerId, itemDto)
        );

        assertEquals("Поле 'доступность' должно быть указано.", exception.getMessage());
    }

    @Test
    @Transactional
    void getItemById_shouldSetLastAndNextBooking_whenUserIsOwner() {
        User owner = userRepository.save(new User(null, "Owner", "owner@example.com"));
        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Ударная");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);

        User booker = userRepository.save(new User(null, "Booker", "booker@example.com"));

        Booking pastBooking = new Booking();
        pastBooking.setItem(item);
        pastBooking.setBooker(booker);
        pastBooking.setStart(LocalDateTime.now().minusDays(5));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        pastBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(pastBooking);

        Booking futureBooking = new Booking();
        futureBooking.setItem(item);
        futureBooking.setBooker(booker);
        futureBooking.setStart(LocalDateTime.now().plusDays(1));
        futureBooking.setEnd(LocalDateTime.now().plusDays(3));
        futureBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(futureBooking);

        ItemDto result = itemService.getItemById(item.getId(), owner.getId());

        assertNotNull(result.getLastBooking(), "lastBooking должен быть установлен");
        assertNotNull(result.getNextBooking(), "nextBooking должен быть установлен");

        assertEquals(pastBooking.getStart(), result.getLastBooking());
        assertEquals(futureBooking.getStart(), result.getNextBooking());
    }

    @Test
    @Transactional
    void addItem_shouldSetItemRequest_whenRequestIdIsProvided() {
        User owner = userRepository.save(new User(null, "Owner", "owner@example.com"));

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("Нужен инструмент");
        itemRequest.setRequester(owner);
        itemRequest = itemRequestRepository.save(itemRequest);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Молоток");
        itemDto.setDescription("Молоток большой");
        itemDto.setAvailable(true);
        itemDto.setRequestId(itemRequest.getId());

        ItemDto result = itemService.addItem(owner.getId(), itemDto);

        assertNotNull(result.getId(), "ID вещи должен быть присвоен");

        Item item = itemRepository.findById(result.getId()).orElseThrow();
        assertNotNull(item.getItemRequest(), "Запрос должен быть привязан к вещи");
        assertEquals(itemRequest.getId(), item.getItemRequest().getId(), "Запрос должен совпадать");
    }
}



