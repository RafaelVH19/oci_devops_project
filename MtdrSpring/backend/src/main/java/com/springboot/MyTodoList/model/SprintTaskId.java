package com.springboot.MyTodoList.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SprintTaskId implements Serializable {

    @Column(name = "SPRINT_ID")
    private Long sprintId;

    @Column(name = "TASK_ID")
    private Long taskId;

    public SprintTaskId() {
    }

    public SprintTaskId(Long sprintId, Long taskId) {
        this.sprintId = sprintId;
        this.taskId = taskId;
    }

    public Long getSprintId() {
        return sprintId;
    }

    public void setSprintId(Long sprintId) {
        this.sprintId = sprintId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SprintTaskId that = (SprintTaskId) o;
        return Objects.equals(sprintId, that.sprintId) && Objects.equals(taskId, that.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sprintId, taskId);
    }
}