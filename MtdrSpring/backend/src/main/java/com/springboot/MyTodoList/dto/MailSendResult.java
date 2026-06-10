package com.springboot.MyTodoList.dto;

public class MailSendResult {

    private final boolean sent;
    private final String errorMessage;

    public MailSendResult(boolean sent, String errorMessage) {
        this.sent = sent;
        this.errorMessage = errorMessage;
    }

    public static MailSendResult ok() {
        return new MailSendResult(true, null);
    }

    public static MailSendResult fail(String errorMessage) {
        return new MailSendResult(false, errorMessage);
    }

    public boolean isSent() {
        return sent;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
