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
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.CommentDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

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
public class ItemServiceTest {
    private final EntityManager em;
    private final ItemService itemService;

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
}

