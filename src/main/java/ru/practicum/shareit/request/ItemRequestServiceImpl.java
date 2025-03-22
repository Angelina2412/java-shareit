package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto createRequest(Long userId, ItemRequestDto requestDto) {
        User user = getUserOrThrow(userId);

        ItemRequest request = new ItemRequest();
        request.setDescription(requestDto.getDescription());
        request.setRequester(user);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = requestRepository.save(request);
        return toItemRequestDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        getUserOrThrow(userId);
        return requestRepository.findByRequesterIdOrderByCreatedDesc(userId)
                .stream()
                .map(this::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ItemDto> getAllRequests(Long userId, int from, int size) {
        getUserOrThrow(userId);

        // Вычисление номера страницы
        int page = from / size;

        Pageable pageable = PageRequest.of(page, size, Sort.by("created").descending());

        Page<Item> itemsPage = itemRepository.findAllExcludingUser(userId, pageable);

        // Преобразуем страницу Item в страницу ItemDto
        return itemsPage.map(this::toItemDto);
    }


    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        getUserOrThrow(userId);
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден."));
        return toItemRequestDto(request);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден."));
    }

    private ItemRequestDto toItemRequestDto(ItemRequest request) {
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                itemRepository.findByItemRequest_Id(request.getId()).stream()
                        .map(item -> new ItemResponseDto(item.getId(), item.getName(), item.getOwner().getId()))
                        .collect(Collectors.toList())
        );
    }

    private ItemDto toItemDto(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.isAvailable());
        return dto;
    }
}
