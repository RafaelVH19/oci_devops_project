package com.springboot.MyTodoList.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entidad de asociación entre un Sprint y una Tarea.
 *
 * Permite registrar la inclusión de una tarea dentro de un Sprint,
 * almacenando además información temporal sobre cuándo fue agregada
 * o removida.
 *
 * Implementa una relación muchos a muchos mediante una clave compuesta.
 */
@Entity
@Table(name = "SPRINT_TASKS")
public class SprintTask {

    @EmbeddedId
    private SprintTaskId id;

    @Column(name = "ADDED_AT", nullable = false)
    private LocalDateTime addedAt;

    @Column(name = "REMOVED_AT")
    private LocalDateTime removedAt;

    /** Constructor por defecto. */
    public SprintTask() {
    }

    /** Obtiene el ID de la relación Sprint-Tarea */
    public SprintTaskId getId() {
        return id;
    }

    /** Establece el ID de la relación Sprint-Tarea */
    public void setId(SprintTaskId id) {
        this.id = id;
    }

    /** Obtiene el ID del Sprint en la relación */
    public Long getSprintId() {
        return id != null ? id.getSprintId() : null;
    }

    /** Establece el ID del Sprint en la relación */
    public void setSprintId(Long sprintId) {
        if (id == null) {
            id = new SprintTaskId();
        }
        id.setSprintId(sprintId);
    }

    /** Obtiene el ID de la Tarea en la relación */
    public Long getTaskId() {
        return id != null ? id.getTaskId() : null;
    }

    /** Establece el ID de la Tarea en la relación */
    public void setTaskId(Long taskId) {
        if (id == null) {
            id = new SprintTaskId();
        }
        id.setTaskId(taskId);
    }

    /** Obtiene la fecha y hora en que la tarea fue agregada al sprint */
    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    /** Establece la fecha y hora en que la tarea fue agregada al sprint */
    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    /** Obtiene la fecha y hora en que la tarea fue removida del sprint */
    public LocalDateTime getRemovedAt() {
        return removedAt;
    }

    /** Establece la fecha y hora en que la tarea fue removida del sprint */
    public void setRemovedAt(LocalDateTime removedAt) {
        this.removedAt = removedAt;
    }
}