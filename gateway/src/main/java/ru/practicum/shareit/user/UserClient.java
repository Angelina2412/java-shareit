package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserClient {

    private static final String API_PREFIX = "/users";
    private final BaseClient baseClient;

    public UserClient(BaseClient baseClient) {
        this.baseClient = baseClient;
    }

    public ResponseEntity<UserDto> createUser(UserDto userDto) {
        ResponseEntity<Object> response = baseClient.post(API_PREFIX, null, null, userDto);

        if (response.getBody() != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            UserDto userResponse = objectMapper.convertValue(response.getBody(), UserDto.class);
            return ResponseEntity.ok(userResponse);
        } else {
            return ResponseEntity.noContent().build();
        }
    }


    public ResponseEntity<UserDto> getUser(Long id) {
        String url = String.format("%s/%d", API_PREFIX, id);
        ResponseEntity<Object> response = baseClient.get(url, null, null);
        if (response.getBody() != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            UserDto userResponse = objectMapper.convertValue(response.getBody(), UserDto.class);
            return ResponseEntity.ok(userResponse);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<Object> responseList = baseClient.getList(API_PREFIX, null);
        ObjectMapper objectMapper = new ObjectMapper();
        List<UserDto> userDtoList = new ArrayList<>();
        for (Object obj : responseList) {
            UserDto userDto = objectMapper.convertValue(obj, UserDto.class);
            userDtoList.add(userDto);
        }
        return ResponseEntity.ok(userDtoList);
    }


    public ResponseEntity<Void> deleteUser(Long id) {
        String url = String.format("%s/%d", API_PREFIX, id);
        baseClient.delete(url, null, null);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<UserDto> updateUser(Long id, UserDto userUpdates, long userId) {
        String url = String.format("%s/%d", API_PREFIX, id);
        baseClient.put(url, userId, null, userUpdates);  // Передаем userId
        return ResponseEntity.ok(userUpdates);
    }

}

