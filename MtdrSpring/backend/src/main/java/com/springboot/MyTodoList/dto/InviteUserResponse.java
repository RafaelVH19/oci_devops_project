package com.springboot.MyTodoList.dto;

import com.springboot.MyTodoList.model.User;

public class InviteUserResponse {

    private User user;
    private String temporaryPassword;
    private boolean authAccountCreated;
    private boolean inviteEmailSent;
    private String inviteEmailError;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }

    public void setTemporaryPassword(String temporaryPassword) {
        this.temporaryPassword = temporaryPassword;
    }

    public boolean isAuthAccountCreated() {
        return authAccountCreated;
    }

    public void setAuthAccountCreated(boolean authAccountCreated) {
        this.authAccountCreated = authAccountCreated;
    }

    public boolean isInviteEmailSent() {
        return inviteEmailSent;
    }

    public void setInviteEmailSent(boolean inviteEmailSent) {
        this.inviteEmailSent = inviteEmailSent;
    }

    public String getInviteEmailError() {
        return inviteEmailError;
    }

    public void setInviteEmailError(String inviteEmailError) {
        this.inviteEmailError = inviteEmailError;
    }
}
