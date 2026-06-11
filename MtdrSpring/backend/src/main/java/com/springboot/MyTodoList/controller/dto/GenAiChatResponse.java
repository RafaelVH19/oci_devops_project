package com.springboot.MyTodoList.controller.dto;

public class GenAiChatResponse {

    private String reply;
    private boolean workspaceChanged;

    public GenAiChatResponse() {
    }

    public GenAiChatResponse(String reply) {
        this(reply, false);
    }

    public GenAiChatResponse(String reply, boolean workspaceChanged) {
        this.reply = reply;
        this.workspaceChanged = workspaceChanged;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public boolean isWorkspaceChanged() {
        return workspaceChanged;
    }

    public void setWorkspaceChanged(boolean workspaceChanged) {
        this.workspaceChanged = workspaceChanged;
    }
}
