package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByEmailIgnoreCase(email.trim());
    }

    public ResponseEntity<User> getUserById(int id) {
        Optional<User> userById = userRepository.findById((long) id);
        if (userById.isPresent()) {
            return new ResponseEntity<>(userById.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    public User addUser(User newUser) {
        encodePasswordIfNeeded(newUser);
        return userRepository.save(newUser);
    }

    public User test() {
        User newUser = new User(88L, "someNumber", "pwd");
        return userRepository.save(newUser);
    }

    public boolean deleteUser(int id) {
        try {
            userRepository.deleteById((long) id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public User updateUser(long id, User user2update) {
        Optional<User> dbUser = userRepository.findById(id);
        if (dbUser.isPresent()) {
            User user = dbUser.get();
            user.setId(id);
            user.setName(user2update.getName());
            user.setEmail(user2update.getEmail());
            user.setTelegramId(user2update.getTelegramId());
            user.setRole(user2update.getRole());
            user.setWorkMode(user2update.getWorkMode());
            user.setIsActive(user2update.getIsActive());
            user.setCreatedAt(user2update.getCreatedAt());
            if (user2update.getPasswordHash() != null && !user2update.getPasswordHash().isBlank()) {
                user.setPasswordHash(user2update.getPasswordHash());
                encodePasswordIfNeeded(user);
            }
            return userRepository.save(user);
        }
        return null;
    }

    private void encodePasswordIfNeeded(User user) {
        String raw = user.getPasswordHash();
        if (raw == null || raw.isBlank() || isBcryptHash(raw)) {
            return;
        }
        user.setPasswordHash(passwordEncoder.encode(raw));
    }

    private static boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
