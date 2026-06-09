package com.springboot.MyTodoList.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;

import java.time.LocalDateTime;

/**
 * Representa un Sprint dentro de la metodología ágil Scrum.
 *
 * Un Sprint define un período de tiempo limitado durante el cual se
 * desarrollan y completan tareas específicas del proyecto.
 *
 * La entidad almacena información relacionada con su nombre,
 * fechas de inicio y fin, así como la fecha de creación.
 */
@Entity
@Table(name = "SPRINTS")
public class Sprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "START_DATE", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "END_DATE", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Inicializa automáticamente la fecha de creación antes de persistir
     * la entidad en la base de datos.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    /** Constructor por defecto */
    public Sprint() {
    }

    /** Obtiene el ID del Sprint */
    public Long getId() {
        return id;
    }

    /** Establece el ID del Sprint */
    public void setId(Long id) {
        this.id = id;
    }

    /** Obtiene el nombre del Sprint */
    public String getName() {
        return name;
    }

    /** Establece el nombre del Sprint */
    public void setName(String name) {
        this.name = name;
    }

    /** Obtiene la fecha de inicio del Sprint */
    public LocalDateTime getStartDate() {
        return startDate;
    }

    /** Establece la fecha de inicio del Sprint */
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    /** Obtiene la fecha de finalización del Sprint */
    public LocalDateTime getEndDate() {
        return endDate;
    }

    /** Establece la fecha de finalización del Sprint */
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    /** Obtiene la fecha de creación del Sprint */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Establece la fecha de creación del Sprint */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}