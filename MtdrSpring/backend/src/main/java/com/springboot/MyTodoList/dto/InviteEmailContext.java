package com.springboot.MyTodoList.dto;

public class InviteEmailContext {

    private final String inviteeName;
    private final String inviteeEmail;
    private final String temporaryPassword;
    private final String inviterName;
    private final String inviterEmail;
    private final String teamName;
    private final String role;
    private final String loginUrl;

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

    public String getInviteeName() {
        return inviteeName;
    }

    public String getInviteeEmail() {
        return inviteeEmail;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }

    public String getInviterName() {
        return inviterName;
    }

    public String getInviterEmail() {
        return inviterEmail;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getRole() {
        return role;
    }

    public String getLoginUrl() {
        return loginUrl;
    }
}
