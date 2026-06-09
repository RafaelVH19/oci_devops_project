package com.springboot.MyTodoList.model;

import com.springboot.MyTodoList.model.enums.TaskPriority;
import com.springboot.MyTodoList.model.enums.TaskStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDateTime;

/**
 * Representa una tarea dentro del sistema de gestión de proyectos.
 *
 * Una tarea contiene información descriptiva, estado, prioridad,
 * responsable asignado y métricas relacionadas con el avance del trabajo.</p>
 *
 * También permite almacenar información para análisis mediante IA,
 * así como datos de estimación y tiempo invertido.</p>
 */
@Entity
@Table(name = "TASKS")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "PRIORITY", nullable = false, length = 20)
    private TaskPriority priority;

    @Column(name = "ASSIGNED_TO", nullable = false)
    private Long assignedTo;

    @Column(name = "CREATED_BY", nullable = false)
    private Long createdBy;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "COMPLETED_DATE")
    private LocalDateTime completedDate;

    @Transient
    float[] insight;

    @Column(name = "HOURS_DONE")
    private Integer hoursDone;

    @Column(name = "EXPECTED_HOURS")
    private Integer expectedHours;

    @Column(name = "IS_BUG", nullable = false)
    private Boolean isBug = false;

    /** Constructor por defecto. */
    public Task() {
    }

    /**
     * Inicializa automáticamente valores por defecto antes
     * de persistir la entidad.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = TaskStatus.PENDING;
        }
        if (this.priority == null) {
            this.priority = TaskPriority.MEDIUM;
        }
    }

    /**
     * Actualiza automáticamente la fecha de modificación
     * cuando la entidad es actualizada.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** Obtiene el ID de la tarea. */
    public Long getId() {
        return id;
    }

    /** Establece el ID de la tarea. */
    public void setId(Long id) {
        this.id = id;
    }

    /** Obtiene el título de la tarea. */
    public String getTitle() {
        return title;
    }

    /** Establece el título de la tarea. */
    public void setTitle(String title) {
        this.title = title;
    }

    /** Obtiene la descripción de la tarea. */
    public String getDescription() {
        return description;
    }

    /** Establece la descripción de la tarea. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Obtiene el estado de la tarea. */
    public TaskStatus getStatus() {
        return status;
    }

    /** Establece el estado de la tarea. */
    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    /** Obtiene la prioridad de la tarea. */
    public TaskPriority getPriority() {
        return priority;
    }

    /** Establece la prioridad de la tarea. */
    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    /** Obtiene el ID del usuario asignado a la tarea. */
    public Long getAssignedTo() {
        return assignedTo;
    }

    /** Establece el ID del usuario asignado a la tarea. */
    public void setAssignedTo(Long assignedTo) {
        this.assignedTo = assignedTo;
    }

    /** Obtiene el ID del usuario que creó la tarea. */
    public Long getCreatedBy() {
        return createdBy;
    }

    /** Establece el ID del usuario que creó la tarea. */
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    /** Obtiene la fecha de creación de la tarea. */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Establece la fecha de creación de la tarea. */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** Obtiene la fecha de actualización de la tarea. */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /** Establece la fecha de actualización de la tarea. */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** Obtiene la fecha de finalización de la tarea. */
    public LocalDateTime getCompletedDate() {
        return completedDate;
    }

    /** Establece la fecha de finalización de la tarea. */
    public void setCompletedDate(LocalDateTime completedDate) {
        this.completedDate = completedDate;
    }

    /** Obtiene el insight generado por IA para la tarea. */
    public float[] getInsight() {
        return insight;
    }

    /** Establece el insight generado por IA para la tarea. */
    public void setInsight(float[] insight) {
        this.insight = insight;
    }

    /** Obtiene las horas dedicadas a la tarea. */
    public Integer getHoursDone() {
        return hoursDone;
    }

    /** Establece las horas dedicadas a la tarea. */
    public void setHoursDone(Integer hoursDone) {
        this.hoursDone = hoursDone;
    }

    /** Obtiene las horas esperadas para la tarea. */
    public Integer getExpectedHours() {
        return expectedHours;
    }

    /** Establece las horas esperadas para la tarea. */
    public void setExpectedHours(Integer expectedHours) {
        this.expectedHours = expectedHours;
    }

    /** Obtiene si la tarea es un error. */
    public Boolean getIsBug() {
        return isBug;
    }

    /** Establece si la tarea es un error. */
    public void setIsBug(Boolean isBug) {
        this.isBug = isBug;
    }
}