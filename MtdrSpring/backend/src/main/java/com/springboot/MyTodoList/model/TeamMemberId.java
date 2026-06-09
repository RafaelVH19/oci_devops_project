package com.springboot.MyTodoList.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clave primaria compuesta utilizada para identificar
 * de manera única la relación entre un equipo y un usuario.
 */
@Embeddable
public class TeamMemberId implements Serializable {

    @Column(name = "TEAM_ID")
    private Long teamId;

    @Column(name = "MEMBER_USER_ID")
    private Long memberUserId;

    /** Constructor por defecto. */
    public TeamMemberId() {
    }

    /** Constructor que inicializa ambos campos de la clave compuesta. */
    public TeamMemberId(Long teamId, Long memberUserId) {
        this.teamId = teamId;
        this.memberUserId = memberUserId;
    }

    /** Obtiene el ID del equipo en la relación. */
    public Long getTeamId() {
        return teamId;
    }

    /** Establece el ID del equipo en la relación. */
    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    /** Obtiene el ID del miembro en la relación. */
    public Long getMemberUserId() {
        return memberUserId;
    }

    /** Establece el ID del miembro en la relación. */
    public void setMemberUserId(Long memberUserId) {
        this.memberUserId = memberUserId;
    }

    /**
     * Determina si dos identificadores compuestos representan
     * la misma relación equipo-miembro.
     */
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

    /**
     * Genera un código hash basado en los atributos que conforman
     * la clave compuesta.
     */
    @Override
    public int hashCode() {
        return Objects.hash(teamId, memberUserId);
    }
}