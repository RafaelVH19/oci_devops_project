package com.springboot.MyTodoList.controller.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase utilizada para enviar una solicitud al servicio de IA.
 * Incluye el mensaje actual, el historial de conversación
 * y el rol del usuario que realiza la petición.
 */
public class GenAiChatRequest {

    private String message;
    private List<GenAiChatMessage> history = new ArrayList<>();
    private String userRole;

    /** Obtiene el mensaje enviado por el usuario */
    public String getMessage() {
        return message;
    }

    /** Establece el mensaje enviado por el usuario */
    public void setMessage(String message) {
        this.message = message;
    }

    /** Obtiene el historial de conversación */
    public List<GenAiChatMessage> getHistory() {
        return history;
    }

    /**
     * Establece el historial de conversación.
     * Si la lista es nula, se inicializa una lista vacía.
     */
    public void setHistory(List<GenAiChatMessage> history) {
        this.history = history == null ? new ArrayList<>() : history;
    }

    /** Obtiene el rol del usuario */
    public String getUserRole() {
        return userRole;
    }

    /** Establece el rol del usuario */
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
}
