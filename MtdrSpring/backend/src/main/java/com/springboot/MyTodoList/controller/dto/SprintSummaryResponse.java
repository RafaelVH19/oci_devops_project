package com.springboot.MyTodoList.controller.dto;

import com.springboot.MyTodoList.model.Sprint;

import java.time.LocalDateTime;

/**
 * Clase que proporciona un resumen simplificado de un sprint.
 * Se utiliza para exponer únicamente la información relevante
 * de un sprint en las respuestas de la API.
 */
public class SprintSummaryResponse {

    private Long id;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    /** Constructor vacío requerido para serialización y deserialización */
    public SprintSummaryResponse() {
    }

    /** Construye una respuesta de resumen a partir de un objeto Sprint */
    public SprintSummaryResponse(Sprint sprint) {
        if (sprint != null) {
            this.id = sprint.getId();
            this.name = sprint.getName();
            this.startDate = sprint.getStartDate();
            this.endDate = sprint.getEndDate();
        }
    }

    /** Obtiene el ID del sprint */
    public Long getId() {
        return id;
    }

    /** Establece el ID del sprint */
    public void setId(Long id) {
        this.id = id;
    }

    /** Obtiene el nombre del sprint */
    public String getName() {
        return name;
    }

    /** Establece el nombre del sprint */
    public void setName(String name) {
        this.name = name;
    }

    /** Obtiene la fecha de inicio del sprint */
    public LocalDateTime getStartDate() {
        return startDate;
    }

    /** Establece la fecha de inicio del sprint */
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    /** Obtiene la fecha de finalización del sprint */
    public LocalDateTime getEndDate() {
        return endDate;
    }

    /** Establece la fecha de finalización del sprint */
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}
