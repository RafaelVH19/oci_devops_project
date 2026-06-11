package com.springboot.MyTodoList.service;

import java.util.ArrayList;
import java.util.List;

public class LumiActionPlan {

    public enum Action {
        NONE,
        CHAT,
        CREATE_TEAM,
        CREATE_PROJECT,
        CREATE_SPRINT,
        CREATE_TASK,
        COMPLETE_TASK,
        WORKLOAD,
        TASK_QUERY
    }

    private Action action = Action.NONE;
    private String teamName;
    private List<String> memberNames = new ArrayList<>();
    private String managerName;
    private String projectName;
    private String sourceTeamName;
    private String sprintName;
    private String startDate;
    private String endDate;
    private String taskTitle;
    private String taskDescription;
    private String assigneeName;
    private Integer expectedHours;
    private String taskPriority;
    private String taskDueDate;
    private Integer hoursDone;
    private boolean needsClarification;
    private String clarificationQuestion;

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action == null ? Action.NONE : action;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public List<String> getMemberNames() {
        return memberNames;
    }

    public void setMemberNames(List<String> memberNames) {
        this.memberNames = memberNames == null ? new ArrayList<>() : memberNames;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSourceTeamName() {
        return sourceTeamName;
    }

    public void setSourceTeamName(String sourceTeamName) {
        this.sourceTeamName = sourceTeamName;
    }

    public String getSprintName() {
        return sprintName;
    }

    public void setSprintName(String sprintName) {
        this.sprintName = sprintName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public Integer getExpectedHours() {
        return expectedHours;
    }

    public void setExpectedHours(Integer expectedHours) {
        this.expectedHours = expectedHours;
    }

    public String getTaskPriority() {
        return taskPriority;
    }

    public void setTaskPriority(String taskPriority) {
        this.taskPriority = taskPriority;
    }

    public String getTaskDueDate() {
        return taskDueDate;
    }

    public void setTaskDueDate(String taskDueDate) {
        this.taskDueDate = taskDueDate;
    }

    public Integer getHoursDone() {
        return hoursDone;
    }

    public void setHoursDone(Integer hoursDone) {
        this.hoursDone = hoursDone;
    }

    public boolean isNeedsClarification() {
        return needsClarification;
    }

    public void setNeedsClarification(boolean needsClarification) {
        this.needsClarification = needsClarification;
    }

    public String getClarificationQuestion() {
        return clarificationQuestion;
    }

    public void setClarificationQuestion(String clarificationQuestion) {
        this.clarificationQuestion = clarificationQuestion;
    }

    public boolean isExecutable() {
        return action != null
            && action != Action.NONE
            && action != Action.CHAT
            && action != Action.TASK_QUERY
            && !needsClarification;
    }
}
