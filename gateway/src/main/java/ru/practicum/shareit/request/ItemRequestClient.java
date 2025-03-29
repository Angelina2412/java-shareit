package ru.practicum.shareit.request;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Service
public class ItemRequestClient {

    private static final String API_PREFIX = "/requests";
    private final BaseClient baseClient;

    public ItemRequestClient(BaseClient baseClient) {
        this.baseClient = baseClient;
    }

    public ResponseEntity<Object> createRequest(Long userId, ItemRequestDto requestDto) {
        return baseClient.post(API_PREFIX, userId, null, requestDto);
    }

    public ResponseEntity<Object> getUserRequests(Long userId) {
        String url = String.format("%s?userId=%d", API_PREFIX, userId);
        return baseClient.get(url, userId, null);
    }

    public ResponseEntity<Object> getAllRequests(Long userId, int from, int size) {
        String url = String.format("%s/all?userId=%d&from=%d&size=%d", API_PREFIX, userId, from, size);
        return baseClient.get(url, userId, null);
    }

    public ResponseEntity<Object> getRequestById(Long userId, Long requestId) {
        String url = String.format("%s/%d?userId=%d", API_PREFIX, requestId, userId);
        return baseClient.get(url, userId, null);
    }
}

