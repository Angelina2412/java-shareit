package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserServiceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@ComponentScan(basePackages = "ru.practicum.shareit")
public class UserServiceTest {
    @Autowired
    private UserRepository userRepository;

    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
        user = new User(null, "Test User", "test@example.com");
        userRepository.save(user);
    }

    @Test
    void addUser_ShouldSaveUser() {
        User newUser = new User(null, "New User", "new@example.com");
        User savedUser = userService.addUser(newUser);

        assertNotNull(savedUser.getId());
        assertEquals("New User", savedUser.getName());
        assertEquals("new@example.com", savedUser.getEmail());
    }

    @Test
    void addUser_ShouldThrowConflictException_WhenEmailExists() {
        User duplicateUser = new User(null, "Another User", "test@example.com");

        assertThrows(ConflictException.class, () -> userService.addUser(duplicateUser));
    }

    @Test
    void updateUser_ShouldUpdateUserDetails() {
        User updates = new User(null, "Updated Name", "updated@example.com");
        User updatedUser = userService.updateUser(user.getId(), updates);

        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@example.com", updatedUser.getEmail());
    }

    @Test
    void updateUser_ShouldThrowNotFoundException_WhenUserNotExists() {
        User updates = new User(null, "Updated Name", "updated@example.com");

        assertThrows(NotFoundException.class, () -> userService.updateUser(999L, updates));
    }

    @Test
    void updateUser_ShouldThrowConflictException_WhenEmailExists() {
        User anotherUser = new User(null, "Another User", "another@example.com");
        userRepository.save(anotherUser);

        User updates = new User(null, "Updated Name", "another@example.com");

        assertThrows(ConflictException.class, () -> userService.updateUser(user.getId(), updates));
    }

    @Test
    void getUserById_ShouldReturnUser() {
        User foundUser = userService.getUserById(user.getId());

        assertEquals(user.getId(), foundUser.getId());
        assertEquals(user.getName(), foundUser.getName());
    }

    @Test
    void getUserById_ShouldThrowNotFoundException_WhenUserNotExists() {
        assertThrows(NotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        List<User> users = userService.getAllUsers();

        assertThat(users).isNotEmpty();
        assertThat(users).hasSize(1);
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        boolean result = userService.deleteUser(user.getId());

        assertTrue(result);
        assertFalse(userRepository.existsById(user.getId()));
    }

    @Test
    void deleteUser_ShouldReturnFalse_WhenUserNotExists() {
        boolean result = userService.deleteUser(999L);

        assertFalse(result);
    }

    @Test
    void emailExists_ShouldReturnTrue_WhenEmailExists() {
        assertTrue(userService.emailExists(user.getEmail()));
    }

    @Test
    void emailExists_ShouldReturnFalse_WhenEmailNotExists() {
        assertFalse(userService.emailExists("nonexistent@example.com"));
    }

    @Test
    void existsById_ShouldReturnTrue_WhenUserExists() {
        assertTrue(userService.existsById(user.getId()));
    }

    @Test
    void existsById_ShouldReturnFalse_WhenUserNotExists() {
        assertFalse(userService.existsById(999L));
    }
}
