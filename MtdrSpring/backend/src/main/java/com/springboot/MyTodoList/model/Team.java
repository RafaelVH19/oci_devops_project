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
 * Representa un equipo de trabajo dentro de la organización.
 *
 * Un equipo está conformado por varios usuarios y posee un líder
 * o responsable identificado mediante el atributo managerId.</p>
 */
@Entity
@Table(name = "TEAMS")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "MANAGER_ID", nullable = false)
    private Long managerId;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Establece automáticamente la fecha de creación del equipo
     * antes de almacenarlo en la base de datos.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    /** Constructor por defecto. */
    public Team() {
    }

    /** Obtiene el ID del equipo. */
    public Long getId() {
        return id;
    }

    /** Establece el ID del equipo. */
    public void setId(Long id) {
        this.id = id;
    }

    /** Obtiene el nombre del equipo. */
    public String getName() {
        return name;
    }

    /** Establece el nombre del equipo. */
    public void setName(String name) {
        this.name = name;
    }

    /** Obtiene el ID del líder del equipo. */
    public Long getManagerId() {
        return managerId;
    }

    /** Establece el ID del líder del equipo. */
    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    /** Obtiene la fecha de creación del equipo. */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Establece la fecha de creación del equipo. */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}