package com.springboot.MyTodoList.dto;

import com.springboot.MyTodoList.model.User;

/**
 * Clase utilizada para devolver el resultado de una operación
 * de invitación de usuario.
 *
 * Incluye información del usuario creado, contraseña temporal
 * y el estado de las operaciones de autenticación y correo.
 */
public class InviteUserResponse {

    private User user;
    private String temporaryPassword;
    private boolean authAccountCreated;
    private boolean inviteEmailSent;
    private String inviteEmailError;

    /** Constructor para generar una respuesta con el resultado completo del proceso de invitación */
    public InviteUserResponse(
            User user,
            String temporaryPassword,
            boolean authAccountCreated,
            boolean inviteEmailSent,
            String inviteEmailError) {
        this.user = user;
        this.temporaryPassword = temporaryPassword;
        this.authAccountCreated = authAccountCreated;
        this.inviteEmailSent = inviteEmailSent;
        this.inviteEmailError = inviteEmailError;
    }

    /** Obtiene el usuario creado */
    public User getUser() {
        return user;
    }

    /** Establece el usuario creado */
    public void setUser(User user) {
        this.user = user;
    }

    /** Obtiene la contraseña temporal */
    public String getTemporaryPassword() {
        return temporaryPassword;
    }

    /** Establece la contraseña temporal */
    public void setTemporaryPassword(String temporaryPassword) {
        this.temporaryPassword = temporaryPassword;
    }

    /** Obtiene el estado de la creación de la cuenta de autenticación */
    public boolean isAuthAccountCreated() {
        return authAccountCreated;
    }

    /** Establece el estado de la creación de la cuenta de autenticación */
    public void setAuthAccountCreated(boolean authAccountCreated) {
        this.authAccountCreated = authAccountCreated;
    }

    /** Obtiene el estado del envío del correo de invitación */
    public boolean isInviteEmailSent() {
        return inviteEmailSent;
    }

    /** Establece el estado del envío del correo de invitación */
    public void setInviteEmailSent(boolean inviteEmailSent) {
        this.inviteEmailSent = inviteEmailSent;
    }

    /** Obtiene el error del envío del correo de invitación, si existe */
    public String getInviteEmailError() {
        return inviteEmailError;
    }

    /** Establece el error del envío del correo de invitación */
    public void setInviteEmailError(String inviteEmailError) {
        this.inviteEmailError = inviteEmailError;
    }
}
