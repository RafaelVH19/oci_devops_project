package com.springboot.MyTodoList.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Entidad que representa la pertenencia de un usuario a un equipo.
 *
 * Implementa una relación muchos a muchos entre usuarios
 * y equipos mediante una clave compuesta.</p>
 */
@Entity
@Table(name = "TEAM_MEMBERS")
public class TeamMember {

    @EmbeddedId
    private TeamMemberId id;

    /** Constructor por defecto. */
    public TeamMember() {
    }

    /** Obtiene el ID de la relación. */
    public TeamMemberId getId() {
        return id;
    }

    /** Establece el ID de la relación. */
    public void setId(TeamMemberId id) {
        this.id = id;
    }

    /** Obtiene el ID del equipo en la relación. */
    public Long getTeamId() {
        return id != null ? id.getTeamId() : null;
    }

    /** Establece el ID del equipo en la relación. */
    public void setTeamId(Long teamId) {
        if (id == null) {
            id = new TeamMemberId();
        }
        id.setTeamId(teamId);
    }

    /** Obtiene el ID del miembro en la relación. */
    public Long getMemberUserId() {
        return id != null ? id.getMemberUserId() : null;
    }

    /** Establece el ID del miembro en la relación. */
    public void setMemberUserId(Long memberUserId) {
        if (id == null) {
            id = new TeamMemberId();
        }
        id.setMemberUserId(memberUserId);
    }
}