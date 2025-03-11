package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserServiceImpl;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;
    private final UserServiceImpl userService;

    public ItemController(ItemService itemService, UserServiceImpl userService) {
        this.itemService = itemService;
        this.userService = userService;
    }

    @PostMapping
    public ItemDto addItem(@RequestHeader(USER_ID_HEADER) Long ownerId, @Valid @RequestBody ItemDto itemDto) throws BadRequestException {
        return itemService.addItem(ownerId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(USER_ID_HEADER) Long ownerId,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) throws AccessDeniedException, BadRequestException {
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
            @RequestBody CommentDto commentDto) throws AccessDeniedException {
        return itemService.addComment(itemId, userId, commentDto);
    }


    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId) {
        ItemDto itemDto = itemService.getItemById(itemId);
        List<CommentDto> comments = itemService.getCommentsByItemId(itemId);
        itemDto.setComments(comments);
        return itemDto;
    }

}
