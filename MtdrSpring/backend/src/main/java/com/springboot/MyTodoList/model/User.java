package com.springboot.MyTodoList.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

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

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "PASSWORD_HASH", nullable = false, length = 255)
    private String passwordHash;

    public User() {
    }

    public User(Long id, String telegramId, String passwordHash) {
        this.id = id;
        this.telegramId = telegramId;
        this.passwordHash = passwordHash;
    }

    public User(long id, String telegramId, String passwordHash) {
        this.id = id;
        this.telegramId = telegramId;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(String telegramId) {
        this.telegramId = telegramId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getWorkMode() {
        return workMode;
    }

    public void setWorkMode(String workMode) {
        this.workMode = workMode;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Long getID() {
        return getId();
    }

    public void setID(long id) {
        setId(id);
    }

    public String getPhoneNumber() {
        return getTelegramId();
    }

    public void setPhoneNumber(String number) {
        setTelegramId(number);
    }

    public String getUserPassword() {
        return getPasswordHash();
    }

    public void setUserPassword(String password) {
        setPasswordHash(password);
    }
}