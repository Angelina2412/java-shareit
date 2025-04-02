package ru.practicum.shareit.item;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.ItemDto;

@Service
public class ItemClient {

    private static final String API_PREFIX = "/items";
    private final BaseClient baseClient;

    public ItemClient(BaseClient baseClient) {
        this.baseClient = baseClient;
    }

    public ResponseEntity<Object> addItem(Long ownerId, ItemDto itemDto) {
        return baseClient.post(API_PREFIX, ownerId, null, itemDto);
    }

    public ResponseEntity<Object> updateItem(Long ownerId, Long itemId, ItemDto itemDto) {
        String url = String.format("%s/%d", API_PREFIX, itemId);
        return baseClient.put(url, ownerId, null, itemDto);
    }

    public ResponseEntity<Object> getAllItemsByOwner(Long ownerId) {
        String url = String.format("%s?ownerId=%d", API_PREFIX, ownerId);
        return baseClient.get(url, ownerId, null);
    }

    public ResponseEntity<Object> searchItems(String text) {
        String url = String.format("%s/search?text=%s", API_PREFIX, text);
        return baseClient.get(url, null, null);
    }

    public ResponseEntity<Object> getItemById(Long userId, Long itemId) {
        String url = String.format("%s/%d", API_PREFIX, itemId);
        return baseClient.get(url, userId, null);
    }

    public ResponseEntity<Object> addComment(Long itemId, Long userId, CommentDto commentDto) {
        String url = String.format("%s/%d/comment", API_PREFIX, itemId);
        return baseClient.post(url, userId, null, commentDto);
    }
}

