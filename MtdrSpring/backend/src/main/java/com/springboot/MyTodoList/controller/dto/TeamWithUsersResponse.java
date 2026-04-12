package com.springboot.MyTodoList.controller.dto;

import com.springboot.MyTodoList.model.Team;

import java.time.LocalDateTime;
import java.util.List;

public class TeamWithUsersResponse {

    private Long id;
    private String name;
    private Long managerId;
    private LocalDateTime createdAt;
    private List<UserSummaryResponse> users;

    public TeamWithUsersResponse() {
    }

    public TeamWithUsersResponse(Team team, List<UserSummaryResponse> users) {
        this.id = team.getId();
        this.name = team.getName();
        this.managerId = team.getManagerId();
        this.createdAt = team.getCreatedAt();
        this.users = users;
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

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<UserSummaryResponse> getUsers() {
        return users;
    }

    public void setUsers(List<UserSummaryResponse> users) {
        this.users = users;
    }
}
