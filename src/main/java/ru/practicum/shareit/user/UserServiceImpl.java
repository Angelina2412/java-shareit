package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User addUser(User user) {
        if (emailExists(user.getEmail())) {
            throw new ConflictException("Email уже существует");
        }

        return userRepository.save(user);
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

        return userRepository.save(existingUser);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User с ID " + id + " не найден"));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email)
                .isPresent();
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }
}
