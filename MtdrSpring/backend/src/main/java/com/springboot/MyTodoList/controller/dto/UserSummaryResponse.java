package com.springboot.MyTodoList.controller.dto;

import com.springboot.MyTodoList.model.User;

/**
 * Clase que proporciona un resumen simplificado de un usuario.
 * Se utiliza para exponer únicamente la información relevante
 * de un usuario en las respuestas de la API.
 */
public class UserSummaryResponse {

    private Long id;
    private String name;
    private String email;
    private String telegramId;
    private String role;
    private String workMode;
    private Integer isActive;

    /** Constructor vacío requerido para serialización y deserialización */
    public UserSummaryResponse() {
    }

    /** Construye una respuesta de resumen a partir de un objeto User */
    public UserSummaryResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.telegramId = user.getTelegramId();
        this.role = user.getRole();
        this.workMode = user.getWorkMode();
        this.isActive = user.getIsActive();
    }

    /** Obtiene el ID del usuario */
    public Long getId() {
        return id;
    }

    /** Establece el ID del usuario */
    public void setId(Long id) {
        this.id = id;
    }

    /** Obtiene el nombre del usuario */
    public String getName() {
        return name;
    }

    /** Establece el nombre del usuario */
    public void setName(String name) {
        this.name = name;
    }

    /** Obtiene el email del usuario */
    public String getEmail() {
        return email;
    }

    /** Establece el email del usuario */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Obtiene el ID de Telegram del usuario */
    public String getTelegramId() {
        return telegramId;
    }

    /** Establece el ID de Telegram del usuario */
    public void setTelegramId(String telegramId) {
        this.telegramId = telegramId;
    }

    /** Obtiene el rol del usuario */
    public String getRole() {
        return role;
    }

    /** Establece el rol del usuario */
    public void setRole(String role) {
        this.role = role;
    }

    /** Obtiene el modo de trabajo del usuario */
    public String getWorkMode() {
        return workMode;
    }

    /** Establece el modo de trabajo del usuario */
    public void setWorkMode(String workMode) {
        this.workMode = workMode;
    }

    /** Obtiene el estado de actividad del usuario */
    public Integer getIsActive() {
        return isActive;
    }

    /** Establece el estado de actividad del usuario */
    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }
}
