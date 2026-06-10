package com.springboot.MyTodoList.dto;

/**
 * Clase que representa el resultado de una operación
 * de envío de correo electrónico.
 *
 * Permite conocer si el correo fue enviado correctamente
 * y almacenar información de error cuando corresponda.
 */
public class MailSendResult {

    private final boolean sent;
    private final String errorMessage;

    /** Constructor para generar un resultado de envío de correo */
    public MailSendResult(boolean sent, String errorMessage) {
        this.sent = sent;
        this.errorMessage = errorMessage;
    }

    /** Crea un resultado de envío de correo exitoso */
    public static MailSendResult ok() {
        return new MailSendResult(true, null);
    }

    /** Crea un resultado de envío de correo fallido */
    public static MailSendResult fail(String errorMessage) {
        return new MailSendResult(false, errorMessage);
    }

    /** Indica si el correo fue enviado correctamente */
    public boolean isSent() {
        return sent;
    }

    /** Obtiene el mensaje de error, si existe */
    public String getErrorMessage() {
        return errorMessage;
    }
}
