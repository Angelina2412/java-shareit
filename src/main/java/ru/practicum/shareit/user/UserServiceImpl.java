package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserServiceImpl implements UserService {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();

    @Override
    public User addUser(User user) {
        if (emailExists(user.getEmail())) {
            throw new ConflictException("Email уже существует");
        }

        if (user.getId() == null) {
            user.setId(idGenerator.incrementAndGet());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(Long id, User userUpdates) {
        User existingUser = getUserById(id);
        if (existingUser == null) {
            throw new NotFoundException("User с ID " + id + " не найден");
        }

        if (userUpdates.getEmail() != null) {
            if (emailExists(userUpdates.getEmail()) &&
                    !existingUser.getEmail().equalsIgnoreCase(userUpdates.getEmail())) {
                throw new ConflictException("Email уже существует");
            }
            existingUser.setEmail(userUpdates.getEmail());
        }

        if (userUpdates.getName() != null) {
            existingUser.setName(userUpdates.getName());
        }

        users.put(existingUser.getId(), existingUser);
        return existingUser;
    }

    @Override
    public User getUserById(Long id) {
        return users.get(id);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public boolean deleteUser(Long id) {
        return users.remove(id) != null;
    }

    @Override
    public boolean emailExists(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public boolean existsById(Long id) {
        return users.containsKey(id);
    }
}
