package com.springboot.MyTodoList.controller.dto;

/**
 * Clase utilizada para devolver la respuesta generada
 * por el servicio de inteligencia artificial.
 */
public class GenAiChatResponse {

    private String reply;

    /** Constructor vacío requerido para serialización y deserialización */
    public GenAiChatResponse() {
    }

    /** Crea una respuesta con el texto generado por la IA */
    public GenAiChatResponse(String reply) {
        this.reply = reply;
    }

    /** Obtiene la respuesta generada por la IA */
    public String getReply() {
        return reply;
    }

    /** Establece la respuesta generada por la IA */
    public void setReply(String reply) {
        this.reply = reply;
    }
}
