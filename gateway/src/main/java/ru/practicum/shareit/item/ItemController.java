package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

@Controller
@RequestMapping(path = "/items")
@Slf4j
@Validated
public class ItemController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    @Autowired
    public ItemController(ItemClient itemClient) {
        this.itemClient = itemClient;
    }

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                          @Valid @RequestBody ItemDto itemDto) {
        log.info("POST /items — addItem called by ownerId={} with body={}", ownerId, itemDto);
        ResponseEntity<Object> response = itemClient.addItem(ownerId, itemDto);
        log.info("POST /items — response: status={}, body={}", response.getStatusCode(), response.getBody());
        return response;
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                             @PathVariable Long itemId,
                                             @RequestBody ItemDto itemDto) {
        log.info("PATCH /items/{} — updateItem called by ownerId={} with body={}", itemId, ownerId, itemDto);
        ResponseEntity<Object> response = itemClient.updateItem(ownerId, itemId, itemDto);
        log.info("PATCH /items/{} — response: status={}, body={}", itemId, response.getStatusCode(), response.getBody());
        return response;
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemsByOwner(@RequestHeader(USER_ID_HEADER) Long ownerId) {
        log.info("GET /items — getAllItemsByOwner called by ownerId={}", ownerId);
        ResponseEntity<Object> response = itemClient.getAllItemsByOwner(ownerId);
        log.info("GET /items — response: status={}, body={}", response.getStatusCode(), response.getBody());
        return response;
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text) {
        log.info("GET /items/search — searchItems called with text={}", text);
        ResponseEntity<Object> response = itemClient.searchItems(text);
        log.info("GET /items/search — response: status={}, body={}", response.getStatusCode(), response.getBody());
        return response;
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@PathVariable Long itemId,
                                             @RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestBody CommentDto commentDto) {
        log.info("POST /items/{}/comment — addComment called by userId={} with body={}", itemId, userId, commentDto);
        ResponseEntity<Object> response = itemClient.addComment(itemId, userId, commentDto);
        log.info("POST /items/{}/comment — response: status={}, body={}", itemId, response.getStatusCode(), response.getBody());
        return response;
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable Long itemId,
                                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET /items/{} — getItemById called by userId={}", itemId, userId);
        ResponseEntity<Object> response = itemClient.getItemById(userId, itemId);
        log.info("GET /items/{} — response: status={}, body={}", itemId, response.getStatusCode(), response.getBody());
        return response;
    }
}

