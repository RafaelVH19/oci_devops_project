package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.TeamMember;
import com.springboot.MyTodoList.model.TeamMemberId;
import com.springboot.MyTodoList.repository.TeamMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/** Servicio encargado de gestionar la lógica de negocio relacionada con los miembros del equipo, incluyendo operaciones CRUD para los miembros del equipo almacenados en la base de datos a través del TeamMemberRepository. */
@Service
public class TeamMemberService {

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    /** Devuelve una lista de todos los miembros del equipo almacenados en la base de datos. */
    public List<TeamMember> findAll() {
        return teamMemberRepository.findAll();
    }

    /** Devuelve un ResponseEntity que contiene el miembro del equipo con el ID especificado si existe, o un estado NOT_FOUND si no se encuentra. */
    public ResponseEntity<TeamMember> getById(TeamMemberId id) {
        Optional<TeamMember> teamMember = teamMemberRepository.findById(id);
        return teamMember.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /** Agrega un nuevo miembro del equipo a la base de datos y devuelve el miembro del equipo guardado, incluyendo su ID generado. */
    public TeamMember add(TeamMember teamMember) {
        return teamMemberRepository.save(teamMember);
    }

    /** Actualiza un miembro del equipo existente en la base de datos y devuelve el miembro del equipo actualizado. */
    public TeamMember update(TeamMemberId id, TeamMember updated) {
        Optional<TeamMember> teamMember = teamMemberRepository.findById(id);
        if (teamMember.isPresent()) {
            TeamMember current = teamMember.get();
            current.setTeamId(updated.getTeamId());
            current.setMemberUserId(updated.getMemberUserId());
            return teamMemberRepository.save(current);
        }
        return null;
    }

    /** Elimina el miembro del equipo con el ID especificado de la base de datos, devolviendo true si la eliminación fue exitosa o false si ocurrió un error. */
    public boolean delete(TeamMemberId id) {
        try {
            teamMemberRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}