package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Controller
@RequestMapping(path = "/users")
@Slf4j
@Validated
public class UserController {

    private final UserClient userClient;

    @Autowired
    public UserController(UserClient userClient) {
        this.userClient = userClient;
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        return userClient.createUser(userDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        return userClient.getUser(id);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return userClient.getAllUsers();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userClient.deleteUser(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userUpdates, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return userClient.updateUser(id, userUpdates, userId);
    }

}


