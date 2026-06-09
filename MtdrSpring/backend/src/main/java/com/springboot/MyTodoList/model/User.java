package com.springboot.MyTodoList.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representa un usuario registrado dentro de la plataforma.
 *
 * La entidad almacena información personal, credenciales,
 * configuración laboral y datos de integración con Telegram.</p>
 *
 * También permite controlar el estado de activación de la cuenta
 * y registrar la fecha de creación.</p>
 */
@Entity
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "EMAIL", nullable = false, length = 150)
    private String email;

    @Column(name = "TELEGRAM_ID", nullable = false, length = 50)
    private String telegramId;

    @Column(name = "ROLE", nullable = false, length = 20)
    private String role;

    @Column(name = "WORK_MODE", nullable = false, length = 20)
    private String workMode;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Integer isActive;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Inicializa automáticamente la fecha de creación al persistir la entidad. */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "PASSWORD_HASH", nullable = false, length = 255)
    private String passwordHash;

    /** Constructor por defecto requerido por JPA. */
    public User() {
    }

    /** Constructor que inicializa todos los campos de la entidad. */
    public User(Long id, String telegramId, String passwordHash) {
        this.id = id;
        this.telegramId = telegramId;
        this.passwordHash = passwordHash;
    }

    /** Constructor que inicializa todos los campos de la entidad. */
    public User(long id, String telegramId, String passwordHash) {
        this.id = id;
        this.telegramId = telegramId;
        this.passwordHash = passwordHash;
    }

    /** Obtiene el ID del usuario. */
    public Long getId() {
        return id;
    }

    /** Establece el ID del usuario. */
    public void setId(Long id) {
        this.id = id;
    }

    /** Obtiene el nombre del usuario. */
    public String getName() {
        return name;
    }

    /** Establece el nombre del usuario. */
    public void setName(String name) {
        this.name = name;
    }

    /** Obtiene el correo electrónico del usuario. */
    public String getEmail() {
        return email;
    }

    /** Establece el correo electrónico del usuario. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Obtiene el ID de Telegram del usuario. */
    public String getTelegramId() {
        return telegramId;
    }

    /** Establece el ID de Telegram del usuario. */
    public void setTelegramId(String telegramId) {
        this.telegramId = telegramId;
    }

    /** Obtiene el rol del usuario. */
    public String getRole() {
        return role;
    }

    /** Establece el rol del usuario. */
    public void setRole(String role) {
        this.role = role;
    }

    /** Obtiene el modo de trabajo del usuario. */
    public String getWorkMode() {
        return workMode;
    }

    /** Establece el modo de trabajo del usuario. */
    public void setWorkMode(String workMode) {
        this.workMode = workMode;
    }

    /** Obtiene el estado de activación del usuario. */
    public Integer getIsActive() {
        return isActive;
    }

    /** Establece el estado de activación del usuario. */
    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    /** Obtiene la fecha de creación del usuario. */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Establece la fecha de creación del usuario. */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** Obtiene el hash de la contraseña del usuario. */
    public String getPasswordHash() {
        return passwordHash;
    }

    /** Establece el hash de la contraseña del usuario. */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}