package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

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
                            INSERT INTO users (id, name, email, telegram_id, role, work_mode, is_active, created_at, password_hash)
                            VALUES (:id, :name, :email, :telegramId, :role, :workMode, :isActive, :createdAt, :passwordHash)
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
