package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager entityManager;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PlatformTransactionManager transactionManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.transactionManager = transactionManager;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByEmailIgnoreCase(email.trim());
    }

    public Optional<User> getUserById(int id) {
        return userRepository.findById((long) id);
    }

    public User addUser(User newUser) {
        encodePasswordIfNeeded(newUser);

        try {
            return userRepository.save(newUser);
        } catch (DataIntegrityViolationException ex) {
            if (isOracleIdIdentityOutOfSync(ex)) {
                return insertUserWithExplicitId(newUser);
            }
            throw ex;
        }
    }

    /**
     * Oracle IDENTITY can stay at 1 while rows already use higher IDs (shared dev DB).
     * Hibernate always inserts id=DEFAULT, so we assign MAX(id)+1 explicitly when that fails.
     */
    private User insertUserWithExplicitId(User newUser) {
        if (newUser.getCreatedAt() == null) {
            newUser.setCreatedAt(LocalDateTime.now());
        }

        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        return tx.execute(status -> {
            Long nextId = userRepository.findNextAvailableId();

            entityManager.createNativeQuery(
                    """
                    INSERT INTO users (
                        id,
                        name,
                        email,
                        telegram_id,
                        role,
                        work_mode,
                        is_active,
                        created_at,
                        password_hash
                    )
                    VALUES (
                        :id,
                        :name,
                        :email,
                        :telegramId,
                        :role,
                        :workMode,
                        :isActive,
                        :createdAt,
                        :passwordHash
                    )
                    """)
                    .setParameter("id", nextId)
                    .setParameter("name", newUser.getName())
                    .setParameter("email", newUser.getEmail())
                    .setParameter("telegramId", newUser.getTelegramId())
                    .setParameter("role", newUser.getRole())
                    .setParameter("workMode", newUser.getWorkMode())
                    .setParameter("isActive", newUser.getIsActive())
                    .setParameter("createdAt", newUser.getCreatedAt())
                    .setParameter("passwordHash", newUser.getPasswordHash())
                    .executeUpdate();

            newUser.setId(nextId);
            return newUser;
        });
    }

    private static boolean isOracleIdIdentityOutOfSync(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();

        if (message == null) {
            return false;
        }

        return message.contains("ORA-00001")
                && (message.contains("columns (ID)") || message.contains("(ID:"));
    }

    public User test() {
        User newUser = new User(88L, "someNumber", "pwd");
        return userRepository.save(newUser);
    }

    public boolean deleteUser(int id) {
        Long userId = (long) id;

        if (!userRepository.existsById(userId)) {
            return false;
        }

        userRepository.deleteById(userId);
        return true;
    }

    public User updateUser(long id, User userToUpdate) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found with id: " + id));

        user.setName(userToUpdate.getName());
        user.setEmail(userToUpdate.getEmail());
        user.setTelegramId(userToUpdate.getTelegramId());
        user.setRole(userToUpdate.getRole());
        user.setWorkMode(userToUpdate.getWorkMode());
        user.setIsActive(userToUpdate.getIsActive());
        user.setCreatedAt(userToUpdate.getCreatedAt());

        if (userToUpdate.getPasswordHash() != null
                && !userToUpdate.getPasswordHash().isBlank()) {

            user.setPasswordHash(userToUpdate.getPasswordHash());
            encodePasswordIfNeeded(user);
        }

        return userRepository.save(user);
    }

    private void encodePasswordIfNeeded(User user) {
        String raw = user.getPasswordHash();

        if (raw == null || raw.isBlank() || isBcryptHash(raw)) {
            return;
        }

        user.setPasswordHash(passwordEncoder.encode(raw));
    }

    private static boolean isBcryptHash(String value) {
        return value.startsWith("$2a$")
                || value.startsWith("$2b$")
                || value.startsWith("$2y$");
    }
}