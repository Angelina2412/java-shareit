package ru.practicum.shareit.user;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();

    @PostConstruct
    public void init() {
        users.put(1L, new User(1L, "User1", "user1@example.com"));
        users.put(2L, new User(2L, "User2", "user2@example.com"));
        users.put(3L, new User(3L, "User3", "user3@example.com"));
    }

    public void addUser(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.incrementAndGet());
        }
        users.put(user.getId(), user);
    }

    public User getUserById(Long id) {
        return users.get(id);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public boolean deleteUser(Long id) {
        return users.remove(id) != null;
    }

    public boolean emailExists(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    public boolean existsById(Long id) {
        return users.containsKey(id);
    }

}
