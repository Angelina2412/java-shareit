package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final String userId = "X-Sharer-User-Id";
    private final ItemService itemService;
    private final UserService userService;

    public ItemController(ItemService itemService, UserService userService) {
        this.itemService = itemService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ItemDto> addItem(@Valid @RequestHeader(userId) Long ownerId,
                                           @Valid @RequestBody ItemDto itemDto) throws BadRequestException {
        if (!userService.existsById(ownerId)) {
            throw new NotFoundException("User с ID " + ownerId + " не найден");
        }
        return new ResponseEntity<>(itemService.addItem(ownerId, itemDto), HttpStatus.CREATED);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(@Valid @RequestHeader(userId) Long ownerId,
                                              @PathVariable Long itemId,
                                              @RequestBody ItemDto itemDto) throws BadRequestException, AccessDeniedException {
        return ResponseEntity.ok(itemService.updateItem(ownerId, itemId, itemDto));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItemById(@PathVariable Long itemId) {
        return ResponseEntity.ok(itemService.getItemById(itemId));
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllItemsByOwner(@RequestHeader(userId) Long ownerId) {
        return ResponseEntity.ok(itemService.getAllItemsByOwner(ownerId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(@RequestParam String text) {
        return ResponseEntity.ok(itemService.searchItems(text));
    }
}
