package ru.practicum.shareit.item;

import org.apache.coyote.BadRequestException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    ItemDto addItem(Long ownerId, ItemDto itemDto) throws BadRequestException;

    ItemDto updateItem(Long ownerId, Long itemId, ItemDto itemDto);

    ItemDto getItemById(Long itemId, Long userId);

    List<ItemDto> getAllItemsByOwner(Long ownerId);

    List<ItemDto> searchItems(String text);

    CommentDto addComment(Long itemId, Long userId, CommentDto commentDto);

    List<CommentDto> getCommentsByItemId(Long itemId);

    Item getItemEntityById(Long itemId, Long userId);
}
