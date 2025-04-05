package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestServiceImpl;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@ComponentScan(basePackages = "ru.practicum.shareit")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceTest {
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    @Autowired
    private ItemRequestServiceImpl itemRequestService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private ItemRepository itemRepository;
    private User user;
    private User owner;
    private Item item;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void setUp() {

        owner = new User(null, "owner", "owner@google.com");
        userRepository.save(owner);

        user = new User(null, "test_user", "user@google.com");
        userRepository.save(user);

        item = new Item("Drill", "Power drill", true, owner);
        itemRepository.save(item);

        itemRequestDto = new ItemRequestDto(null, "Request description", LocalDateTime.now(), Collections.emptyList());
    }

    @Test
    void createRequest_ShouldCreateItemRequest() {
        ItemRequestDto createdRequest = itemRequestService.createRequest(user.getId(), itemRequestDto);

        assertThat(createdRequest).isNotNull();
        assertThat(createdRequest.getDescription()).isEqualTo(itemRequestDto.getDescription());
    }

    @Test
    void getUserRequests_ShouldReturnUserRequests() {
        itemRequestService.createRequest(user.getId(), itemRequestDto);
        List<ItemRequestDto> userRequests = itemRequestService.getUserRequests(user.getId());

        assertThat(userRequests).hasSize(1);
        assertThat(userRequests.get(0).getDescription()).isEqualTo(itemRequestDto.getDescription());
    }

    @Test
    void getRequestById_ShouldReturnItemRequest() {
        ItemRequestDto createdRequest = itemRequestService.createRequest(user.getId(), itemRequestDto);
        ItemRequestDto requestById = itemRequestService.getRequestById(user.getId(), createdRequest.getId());

        assertThat(requestById).isNotNull();
        assertThat(requestById.getDescription()).isEqualTo(createdRequest.getDescription());
    }

    @Test
    void getRequestById_ShouldThrowNotFoundException_WhenRequestNotFound() {
        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(user.getId(), 999L));
    }

    @Test
    void createRequest_ShouldFailWhenUserDoesNotExist() {
        User invalidUser = new User(999L, "invalid_user", "invalid@yandex.ru");
        assertThrows(NotFoundException.class, () -> itemRequestService.createRequest(invalidUser.getId(), itemRequestDto));
    }

    @Test
    void getUserRequests_ShouldReturnEmptyList_WhenUserHasNoRequests() {
        List<ItemRequestDto> userRequests = itemRequestService.getUserRequests(user.getId());

        assertThat(userRequests).isEmpty();
    }

    @Test
    void toItemDto_ShouldConvertItemToItemDto() {
        Item item = new Item("Drill", "Power drill", true, user);

        ItemDto itemDto = itemRequestService.toItemDto(item);

        assertThat(itemDto.getId()).isEqualTo(item.getId());
        assertThat(itemDto.getName()).isEqualTo(item.getName());
        assertThat(itemDto.getDescription()).isEqualTo(item.getDescription());
        assertThat(itemDto.getAvailable()).isEqualTo(item.isAvailable());
    }

    @Test
    void getAllRequests_ShouldReturnItemsNotOwnedByUser() {
        ItemRequestDto createdRequest = itemRequestService.createRequest(user.getId(), itemRequestDto);

        Item requestedItem = new Item("Requested Item", "Item description", true, owner);
        itemRepository.save(requestedItem);

        Item anotherItem = new Item("Other", "Other description", true, owner);
        itemRepository.save(anotherItem);

        Page<ItemDto> result = itemRequestService.getAllRequests(user.getId(), 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    void constructor_ShouldCreateItemRequestWithDescriptionAndRequester() {
        String description = "Картошка";
        LocalDateTime before = LocalDateTime.now();

        ItemRequest request = new ItemRequest(description, user);

        LocalDateTime after = LocalDateTime.now();

        assertThat(request.getDescription()).isEqualTo(description);
        assertThat(request.getRequester()).isEqualTo(user);
        assertThat(request.getCreated()).isNotNull();
        assertThat(!request.getCreated().isBefore(before) && !request.getCreated().isAfter(after)).isTrue();
        assertThat(request.getItems()).isEmpty();
    }
}
