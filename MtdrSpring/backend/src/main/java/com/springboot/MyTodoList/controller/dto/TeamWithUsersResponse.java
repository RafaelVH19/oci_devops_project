package com.springboot.MyTodoList.controller.dto;

import com.springboot.MyTodoList.model.Team;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Clase que representa la respuesta de la API para obtener un equipo
 * junto con la información de sus usuarios. Se utiliza para exponer
 * únicamente la información relevante del equipo y sus miembros.
 */
public class TeamWithUsersResponse {

    private Long id;
    private String name;
    private Long managerId;
    private LocalDateTime createdAt;
    private List<UserSummaryResponse> users;

    /** Constructor vacío requerido para serialización y deserialización */
    public TeamWithUsersResponse() {
    }

    /** Construye una respuesta de equipo con usuarios a partir de un objeto Team y una lista de usuarios */
    public TeamWithUsersResponse(Team team, List<UserSummaryResponse> users) {
        this.id = team.getId();
        this.name = team.getName();
        this.managerId = team.getManagerId();
        this.createdAt = team.getCreatedAt();
        this.users = users;
    }

    /** Obtiene el ID del equipo */
    public Long getId() {
        return id;
    }

    /** Establece el ID del equipo */
    public void setId(Long id) {
        this.id = id;
    }

    /** Obtiene el nombre del equipo */
    public String getName() {
        return name;
    }

    /** Establece el nombre del equipo */
    public void setName(String name) {
        this.name = name;
    }

    /** Obtiene el ID del manager del equipo */
    public Long getManagerId() {
        return managerId;
    }

    /** Establece el ID del manager del equipo */
    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    /** Obtiene la fecha de creación del equipo */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Establece la fecha de creación del equipo */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** Obtiene la lista de usuarios asociados al equipo */ 
    public List<UserSummaryResponse> getUsers() {
        return users;
    }

    /** Establece la lista de usuarios asociados al equipo */
    public void setUsers(List<UserSummaryResponse> users) {
        this.users = users;
    }
}
