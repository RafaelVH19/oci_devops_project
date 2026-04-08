package com.springboot.MyTodoList.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "SPRINT_TASKS")
public class SprintTask {

    @EmbeddedId
    private SprintTaskId id;

    @Column(name = "ADDED_AT", nullable = false)
    private LocalDateTime addedAt;

    @Column(name = "REMOVED_AT")
    private LocalDateTime removedAt;

    public SprintTask() {
    }

    public SprintTaskId getId() {
        return id;
    }

    public void setId(SprintTaskId id) {
        this.id = id;
    }

    public Long getSprintId() {
        return id != null ? id.getSprintId() : null;
    }

    public void setSprintId(Long sprintId) {
        if (id == null) {
            id = new SprintTaskId();
        }
        id.setSprintId(sprintId);
    }

    public Long getTaskId() {
        return id != null ? id.getTaskId() : null;
    }

    public void setTaskId(Long taskId) {
        if (id == null) {
            id = new SprintTaskId();
        }
        id.setTaskId(taskId);
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public LocalDateTime getRemovedAt() {
        return removedAt;
    }

    public void setRemovedAt(LocalDateTime removedAt) {
        this.removedAt = removedAt;
    }
}