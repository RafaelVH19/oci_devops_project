package com.springboot.MyTodoList.controller.dto;

/**
 * Clase que representa un mensaje dentro del historial de conversación
 * utilizado por el módulo de inteligencia artificial.
 * Contiene el rol del participante y el contenido del mensaje.
 */
public class GenAiChatMessage {

    private String role;
    private String content;

    /** Constructor vacío requerido para serialización y deserialización */
    public GenAiChatMessage() {
    }

    /** Obtiene el rol del participante */
    public String getRole() {
        return role;
    }

    /** Establece el rol asociado al mensaje */
    public void setRole(String role) {
        this.role = role;
    }

    /** Obtiene el contenido del mensaje */
    public String getContent() {
        return content;
    }

    /** Establece el contenido del mensaje */
    public void setContent(String content) {
        this.content = content;
    }
}
