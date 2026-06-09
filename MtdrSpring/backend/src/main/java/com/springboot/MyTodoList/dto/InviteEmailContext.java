package com.springboot.MyTodoList.dto;

/**
 * Clase que contiene toda la información necesaria para construir
 * y enviar un correo electrónico de invitación a un nuevo usuario.
 *
 * Incluye datos del usuario invitado, información del invitador,
 * equipo asignado, rol y enlace de acceso a la aplicación.
 */
public class InviteEmailContext {

    private final String inviteeName;
    private final String inviteeEmail;
    private final String temporaryPassword;
    private final String inviterName;
    private final String inviterEmail;
    private final String teamName;
    private final String role;
    private final String loginUrl;

    /** Constructor que inicializa todos los campos necesarios para el contexto del correo de invitación */
    public InviteEmailContext(
            String inviteeName,
            String inviteeEmail,
            String temporaryPassword,
            String inviterName,
            String inviterEmail,
            String teamName,
            String role,
            String loginUrl) {
        this.inviteeName = inviteeName;
        this.inviteeEmail = inviteeEmail;
        this.temporaryPassword = temporaryPassword;
        this.inviterName = inviterName;
        this.inviterEmail = inviterEmail;
        this.teamName = teamName;
        this.role = role;
        this.loginUrl = loginUrl;
    }

    /** Obtiene el nombre del usuario invitado */
    public String getInviteeName() {
        return inviteeName;
    }

    /** Obtiene el correo electrónico del usuario invitado */
    public String getInviteeEmail() {
        return inviteeEmail;
    }

    /** Obtiene la contraseña temporal del usuario invitado */
    public String getTemporaryPassword() {
        return temporaryPassword;
    }

    /** Obtiene el nombre del usuario que realiza la invitación */
    public String getInviterName() {
        return inviterName;
    }
        
    /** Obtiene el correo electrónico del usuario que realiza la invitación */
    public String getInviterEmail() {
        return inviterEmail;
    }

    /** Obtiene el nombre del equipo asignado */
    public String getTeamName() {
        return teamName;
    }

    /** Obtiene el rol del usuario invitado */
    public String getRole() {
        return role;
    }

    /** Obtiene el enlace de acceso a la aplicación */
    public String getLoginUrl() {
        return loginUrl;
    }
}
