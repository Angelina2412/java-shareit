package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserServiceImpl;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private final UserServiceImpl userService;
    private final List<Item> items = new ArrayList<>();
    private long idCounter = 1;

    public ItemServiceImpl(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Override
    public ItemDto addItem(Long ownerId, ItemDto itemDto) {
        if (!userService.existsById(ownerId)) {
            throw new NotFoundException("Пользователь с ID " + ownerId + " не найден.");
        }

        if (itemDto.getAvailable() == null) {
            throw new BadRequestException("Поле 'доступность' должно быть указано.");
        }

        Item item = new Item();
        item.setId(idCounter++);
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwnerId(ownerId);

        items.add(item);
        return toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long ownerId, Long itemId, ItemDto itemDto) throws AccessDeniedException {
        Item item = items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Item с ID " + itemId + " не найден."));

        if (!item.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Вы не являетесь владельцем этого Item.");
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return toItemDto(item);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        return items.stream()
                .filter(item -> item.getId().equals(itemId))
                .map(this::toItemDto)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Item с ID " + itemId + " не найден."));
    }

    @Override
    public List<ItemDto> getAllItemsByOwner(Long ownerId) {
        return items.stream()
                .filter(item -> item.getOwnerId().equals(ownerId))
                .map(this::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream()
                .filter(item -> (item.getName().toLowerCase().contains(text.toLowerCase()) ||
                        item.getDescription().toLowerCase().contains(text.toLowerCase())) &&
                        item.isAvailable())
                .map(this::toItemDto)
                .collect(Collectors.toList());
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
