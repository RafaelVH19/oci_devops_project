package com.springboot.MyTodoList.controller.dto;

import com.springboot.MyTodoList.model.Sprint;

import java.time.LocalDateTime;

public class SprintSummaryResponse {

    private Long id;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public SprintSummaryResponse() {
    }

    public SprintSummaryResponse(Sprint sprint) {
        if (sprint != null) {
            this.id = sprint.getId();
            this.name = sprint.getName();
            this.startDate = sprint.getStartDate();
            this.endDate = sprint.getEndDate();
        }
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

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}
