package com.springboot.MyTodoList.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clave primaria compuesta utilizada por la entidad SprintTask.
 *
 * La combinación de Sprint y Tarea identifica de manera única
 * una relación entre ambos elementos.</p>
 */
@Embeddable
public class SprintTaskId implements Serializable {

    @Column(name = "SPRINT_ID")
    private Long sprintId;

    @Column(name = "TASK_ID")
    private Long taskId;

    /** Constructor por defecto. */
    public SprintTaskId() {
    }

    /** Constructor que inicializa ambos campos de la clave compuesta. */
    public SprintTaskId(Long sprintId, Long taskId) {
        this.sprintId = sprintId;
        this.taskId = taskId;
    }

    /** Obtiene el ID del Sprint en la relación */
    public Long getSprintId() {
        return sprintId;
    }

    /** Establece el ID del Sprint en la relación */
    public void setSprintId(Long sprintId) {
        this.sprintId = sprintId;
    }

    /** Obtiene el ID de la Tarea en la relación */
    public Long getTaskId() {
        return taskId;
    }

    /** Establece el ID de la Tarea en la relación */
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    /**
     * Determina si dos identificadores compuestos representan
     * la misma relación Sprint-Tarea.
     */
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

    /**
     * Genera un código hash basado en los atributos que conforman
     * la clave compuesta.
     */
    @Override
    public int hashCode() {
        return Objects.hash(sprintId, taskId);
    }
}