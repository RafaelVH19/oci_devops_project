package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Team;
import com.springboot.MyTodoList.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/** Servicio encargado de gestionar la lógica de negocio relacionada con los equipos, incluyendo operaciones CRUD para los equipos almacenados en la base de datos a través del TeamRepository. */
@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    /** Devuelve una lista de todos los equipos almacenados en la base de datos. */
    public List<Team> findAll() {
        return teamRepository.findAll();
    }

    /** Devuelve un ResponseEntity que contiene el equipo con el ID especificado si existe, o un estado NOT_FOUND si no se encuentra. */
    public ResponseEntity<Team> getById(Long id) {
        Optional<Team> team = teamRepository.findById(id);
        return team.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /** Agrega un nuevo equipo a la base de datos y devuelve el equipo guardado, incluyendo su ID generado. */
    public Team add(Team team) {
        return teamRepository.save(team);
    }

    /** Actualiza un equipo existente en la base de datos y devuelve el equipo actualizado. */
    public Team update(Long id, Team updated) {
        Optional<Team> team = teamRepository.findById(id);
        if (team.isPresent()) {
            Team current = team.get();
            current.setName(updated.getName());
            current.setManagerId(updated.getManagerId());
            current.setCreatedAt(updated.getCreatedAt());
            return teamRepository.save(current);
        }
        return null;
    }

    /** Elimina el equipo con el ID especificado de la base de datos, devolviendo true si la eliminación fue exitosa o false si ocurrió un error. */
    public boolean delete(Long id) {
        try {
            teamRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}