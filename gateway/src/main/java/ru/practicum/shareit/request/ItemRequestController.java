package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Controller
@RequestMapping(path = "/requests")
@Slf4j
@Validated
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @Autowired
    public ItemRequestController(ItemRequestClient itemRequestClient) {
        this.itemRequestClient = itemRequestClient;
    }

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestBody ItemRequestDto requestDto,
                                                @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("POST /requests — createRequest called by userId={} with body={}", userId, requestDto);
        ResponseEntity<Object> response = itemRequestClient.createRequest(userId, requestDto);
        log.info("POST /requests — response: status={}, body={}", response.getStatusCode(), response.getBody());
        return response;
    }

    @GetMapping
    public ResponseEntity<Object> getUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET /requests — getUserRequests called by userId={}", userId);
        ResponseEntity<Object> response = itemRequestClient.getUserRequests(userId);
        log.info("GET /requests — response: status={}, body={}", response.getStatusCode(), response.getBody());
        return response;
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @RequestParam(defaultValue = "0") int from,
                                                 @RequestParam(defaultValue = "10") int size) {
        log.info("GET /requests/all — getAllRequests called by userId={} with params: from={}, size={}", userId, from, size);
        ResponseEntity<Object> response = itemRequestClient.getAllRequests(userId, from, size);
        log.info("GET /requests/all — response: status={}, body={}", response.getStatusCode(), response.getBody());
        return response;
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable Long requestId) {
        log.info("GET /requests/{} — getRequestById called by userId={}", requestId, userId);
        ResponseEntity<Object> response = itemRequestClient.getRequestById(userId, requestId);
        log.info("GET /requests/{} — response: status={}, body={}", requestId, response.getStatusCode(), response.getBody());
        return response;
    }
}



