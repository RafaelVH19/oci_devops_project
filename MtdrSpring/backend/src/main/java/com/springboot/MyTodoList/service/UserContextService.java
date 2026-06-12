package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserContextService {

    private final UserRepository userRepository;

    public UserContextService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads the application user from Oracle based on email.
     * This is the single source of truth for identity resolution.
     */
    public User loadByEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found in Oracle: " + email)
                );

        // Optional safety checks (recommended)
        if (user.getIsActive() == null || user.getIsActive() != 1) {
            throw new RuntimeException("User is inactive: " + email);
        }

        return user;
    }
}