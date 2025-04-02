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
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
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
public class ItemRequestService {
    @Autowired
    private ItemRequestServiceImpl itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User user;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void setUp() {
        user = new User(null, "test_user", "user@example.com");
        userRepository.save(user);

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
}
