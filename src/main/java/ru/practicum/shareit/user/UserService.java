package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {
    User addUser(User user);

    User getUserById(Long id);

    List<User> getAllUsers();

    boolean deleteUser(Long id);

    boolean emailExists(String email);

    boolean existsById(Long id);

    User updateUser(Long id, User user);
}
