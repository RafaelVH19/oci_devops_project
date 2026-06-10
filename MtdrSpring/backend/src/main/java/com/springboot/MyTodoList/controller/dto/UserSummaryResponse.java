package com.springboot.MyTodoList.controller.dto;

import com.springboot.MyTodoList.model.User;

public class UserSummaryResponse {

    private Long id;
    private String name;
    private String email;
    private String telegramId;
    private String role;
    private String workMode;
    private Integer isActive;

    public UserSummaryResponse() {
    }

    public UserSummaryResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.telegramId = user.getTelegramId();
        this.role = user.getRole();
        this.workMode = user.getWorkMode();
        this.isActive = user.getIsActive();
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
}
