package com.springboot.MyTodoList.controller.dto;

import java.util.ArrayList;
import java.util.List;

public class GenAiChatRequest {

    private String message;
    private List<GenAiChatMessage> history = new ArrayList<>();
    private String userRole;
    private String userName;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<GenAiChatMessage> getHistory() {
        return history;
    }

    public void setHistory(List<GenAiChatMessage> history) {
        this.history = history == null ? new ArrayList<>() : history;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
