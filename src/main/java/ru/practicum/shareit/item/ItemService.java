package ru.practicum.shareit.item;

import org.apache.coyote.BadRequestException;
import ru.practicum.shareit.item.dto.ItemDto;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface ItemService {
    ItemDto addItem(Long ownerId, ItemDto itemDto) throws BadRequestException;

    ItemDto updateItem(Long ownerId, Long itemId, ItemDto itemDto) throws BadRequestException, AccessDeniedException;

    ItemDto getItemById(Long itemId);

    List<ItemDto> getAllItemsByOwner(Long ownerId);

    List<ItemDto> searchItems(String text);

}
