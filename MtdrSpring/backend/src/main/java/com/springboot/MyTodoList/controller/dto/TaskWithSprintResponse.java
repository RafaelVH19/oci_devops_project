package com.springboot.MyTodoList.controller.dto;

import com.springboot.MyTodoList.model.Task;

import java.time.LocalDateTime;

/**
 * Clase que representa la respuesta de una tarea junto con un resumen del sprint al que pertenece.
 * Se utiliza para exponer la información de una tarea y su sprint asociado en las respuestas de la API.
 */
public class TaskWithSprintResponse {

    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Long assignedTo;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedDate;
    private float[] insight;
    private Integer hoursDone;
    private Integer expectedHours;
    private Boolean isBug;
    private SprintSummaryResponse sprint;

    /** Constructor vacío requerido para serialización y deserialización */
    public TaskWithSprintResponse() {
    }

    /** Construye una respuesta de tarea con sprint a partir de un objeto Task y un resumen de Sprint */
    public TaskWithSprintResponse(Task task, SprintSummaryResponse sprint) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus() != null ? task.getStatus().name() : null;
        this.priority = task.getPriority() != null ? task.getPriority().name() : null;
        this.assignedTo = task.getAssignedTo();
        this.createdBy = task.getCreatedBy();
        this.createdAt = task.getCreatedAt();
        this.updatedAt = task.getUpdatedAt();
        this.completedDate = task.getCompletedDate();
        this.insight = task.getInsight();
        this.hoursDone = task.getHoursDone();
        this.expectedHours = task.getExpectedHours();
        this.isBug = task.getIsBug();
        this.sprint = sprint;
    }

    /** Obtiene las horas completadas */
    public Integer getHoursDone() {
        return hoursDone;
    }

    /** Establece las horas completadas */
    public void setHoursDone(Integer hoursDone) {
        this.hoursDone = hoursDone;
    }

    /** Obtiene las horas esperadas */
    public Integer getExpectedHours() {
        return expectedHours;
    }

    /** Establece las horas esperadas */
    public void setExpectedHours(Integer expectedHours) {
        this.expectedHours = expectedHours;
    }

    /** Obtiene el ID de la tarea */
    public Long getId() {
        return id;
    }

    /** Establece el ID de la tarea */
    public void setId(Long id) {
        this.id = id;
    }

    /** Obtiene el título de la tarea */
    public String getTitle() {
        return title;
    }

    /** Establece el título de la tarea */
    public void setTitle(String title) {
        this.title = title;
    }

    /** Obtiene la descripción de la tarea */
    public String getDescription() {
        return description;
    }

    /** Establece la descripción de la tarea */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Obtiene el estado de la tarea */
    public String getStatus() {
        return status;
    }

    /** Establece el estado de la tarea */
    public void setStatus(String status) {
        this.status = status;
    }

    /** Obtiene la prioridad de la tarea */
    public String getPriority() {
        return priority;
    }

    /** Establece la prioridad de la tarea */
    public void setPriority(String priority) {
        this.priority = priority;
    }

    /** Obtiene el ID del usuario asignado a la tarea */
    public Long getAssignedTo() {
        return assignedTo;
    }

    /** Establece el ID del usuario asignado a la tarea */
    public void setAssignedTo(Long assignedTo) {
        this.assignedTo = assignedTo;
    }

    /** Obtiene el ID del usuario que creó la tarea */
    public Long getCreatedBy() {
        return createdBy;
    }

    /** Establece el ID del usuario que creó la tarea */
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    /** Obtiene si la tarea es un bug */
    public Boolean getIsBug() {
        return isBug;
    }

    /** Establece si la tarea es un bug */
    public void setIsBug(Boolean isBug) {
        this.isBug = isBug;
    }

    /** Obtiene la fecha de creación de la tarea */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Establece la fecha de creación de la tarea */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** Obtiene la fecha de actualización de la tarea */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /** Establece la fecha de actualización de la tarea */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** Obtiene la fecha de finalización de la tarea */
    public LocalDateTime getCompletedDate() {
        return completedDate;
    }

    /** Establece la fecha de finalización de la tarea */
    public void setCompletedDate(LocalDateTime completedDate) {
        this.completedDate = completedDate;
    }

    /** Obtiene los insights de la tarea */
    public float[] getInsight() {
        return insight;
    }

    /** Establece los insights de la tarea */
    public void setInsight(float[] insight) {
        this.insight = insight;
    }

    /** Obtiene la información resumida del sprint */
    public SprintSummaryResponse getSprint() {
        return sprint;
    }

    /** Establece la información resumida del sprint */
    public void setSprint(SprintSummaryResponse sprint) {
        this.sprint = sprint;
    }
}
