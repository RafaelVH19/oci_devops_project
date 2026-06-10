package com.springboot.MyTodoList.dto;

/**
 * Clase utilizada para recibir la información necesaria
 * para invitar a un nuevo usuario al sistema.
 *
 * Contiene los datos básicos del usuario, información
 * del invitador y parámetros de configuración inicial.
 */
public class InviteUserRequest {

    private String name;
    private String email;
    private String role = "DEVELOPER";
    private String workMode = "REMOTE";
    private String invitedByName;
    private String invitedByEmail;
    private String teamName;
    private String appOrigin;

    /** Obtiene el nombre del usuario invitado */
    public String getName() {
        return name;
    }

    /** Establece el nombre del usuario invitado */
    public void setName(String name) {
        this.name = name;
    }

    /** Obtiene el correo electrónico del usuario invitado */
    public String getEmail() {
        return email;
    }

    /** Establece el correo electrónico del usuario invitado */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Obtiene el rol del usuario invitado */
    public String getRole() {
        return role;
    }

    /** Establece el rol del usuario invitado */
    public void setRole(String role) {
        this.role = role;
    }

    /** Obtiene el modo de trabajo del usuario invitado */
    public String getWorkMode() {
        return workMode;
    }

    /** Establece el modo de trabajo del usuario invitado */
    public void setWorkMode(String workMode) {
        this.workMode = workMode;
    }

    /** Obtiene el nombre del usuario que realiza la invitación */
    public String getInvitedByName() {
        return invitedByName;
    }

    /** Establece el nombre del usuario que realiza la invitación */
    public void setInvitedByName(String invitedByName) {
        this.invitedByName = invitedByName;
    }

    /** Obtiene el correo electrónico del usuario que realiza la invitación */
    public String getInvitedByEmail() {
        return invitedByEmail;
    }

    /** Establece el correo electrónico del usuario que realiza la invitación */
    public void setInvitedByEmail(String invitedByEmail) {
        this.invitedByEmail = invitedByEmail;
    }

    /** Obtiene el nombre del equipo al que se asignará el usuario invitado */
    public String getTeamName() {
        return teamName;
    }

    /** Establece el nombre del equipo al que se asignará el usuario invitado */
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    /** Obtiene el origen de la aplicación */
    public String getAppOrigin() {
        return appOrigin;
    }

    /** Establece el origen de la aplicación */
    public void setAppOrigin(String appOrigin) {
        this.appOrigin = appOrigin;
    }
}
