package com.springboot.MyTodoList.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TeamMemberId implements Serializable {

    @Column(name = "TEAM_ID")
    private Long teamId;

    @Column(name = "MEMBER_USER_ID")
    private Long memberUserId;

    public TeamMemberId() {
    }

    public TeamMemberId(Long teamId, Long memberUserId) {
        this.teamId = teamId;
        this.memberUserId = memberUserId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Long getMemberUserId() {
        return memberUserId;
    }

    public void setMemberUserId(Long memberUserId) {
        this.memberUserId = memberUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TeamMemberId that = (TeamMemberId) o;
        return Objects.equals(teamId, that.teamId) && Objects.equals(memberUserId, that.memberUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, memberUserId);
    }
}