package ru.practicum.shareit.item;

import org.apache.coyote.BadRequestException;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto addItem(@RequestHeader(USER_ID_HEADER) Long ownerId, @RequestBody ItemDto itemDto) throws BadRequestException {
        return itemService.addItem(ownerId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(USER_ID_HEADER) Long ownerId,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {
        return itemService.updateItem(ownerId, itemId, itemDto);
    }

    @GetMapping
    public List<ItemDto> getAllItemsByOwner(@RequestHeader(USER_ID_HEADER) Long ownerId) {
        List<ItemDto> items = itemService.getAllItemsByOwner(ownerId);
        for (ItemDto itemDto : items) {
            List<CommentDto> comments = itemService.getCommentsByItemId(itemDto.getId());
            itemDto.setComments(comments);
        }
        return items;
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        return itemService.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody CommentDto commentDto) {
        return itemService.addComment(itemId, userId, commentDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId,
                               @RequestHeader("X-Sharer-User-Id") Long userId) {
        ItemDto itemDto = itemService.getItemById(itemId, userId);
        List<CommentDto> comments = itemService.getCommentsByItemId(itemId);
        itemDto.setComments(comments);
        return itemDto;
    }
}
