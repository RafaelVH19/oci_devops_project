package com.springboot.MyTodoList.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "TEAM_MEMBERS")
public class TeamMember {

    @EmbeddedId
    private TeamMemberId id;

    public TeamMember() {
    }

    public TeamMemberId getId() {
        return id;
    }

    public void setId(TeamMemberId id) {
        this.id = id;
    }

    public Long getTeamId() {
        return id != null ? id.getTeamId() : null;
    }

    public void setTeamId(Long teamId) {
        if (id == null) {
            id = new TeamMemberId();
        }
        id.setTeamId(teamId);
    }

    public Long getMemberUserId() {
        return id != null ? id.getMemberUserId() : null;
    }

    public void setMemberUserId(Long memberUserId) {
        if (id == null) {
            id = new TeamMemberId();
        }
        id.setMemberUserId(memberUserId);
    }
}