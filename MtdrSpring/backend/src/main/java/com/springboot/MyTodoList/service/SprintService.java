package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.repository.SprintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/** Servicio encargado de gestionar la lógica de negocio relacionada con los sprints, incluyendo operaciones CRUD para los sprints almacenados en la base de datos a través del SprintRepository. */    
@Service
public class SprintService {

    @Autowired
    private SprintRepository sprintRepository;

    /** Devuelve una lista de todos los sprints almacenados en la base de datos. */
    public List<Sprint> findAll() {
        return sprintRepository.findAll();
    }

    /** Devuelve un ResponseEntity que contiene el sprint con el ID especificado si existe, o un estado NOT_FOUND si no se encuentra. */
    public ResponseEntity<Sprint> getById(Long id) {
        Optional<Sprint> sprint = sprintRepository.findById(id);
        return sprint.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /** Agrega un nuevo sprint a la base de datos y devuelve el sprint guardado, incluyendo su ID generado. */
    public Sprint add(Sprint sprint) {
        return sprintRepository.save(sprint);
    }

    /** Actualiza un sprint existente en la base de datos y devuelve el sprint actualizado. */
    public Sprint update(Long id, Sprint updated) {
        Optional<Sprint> sprint = sprintRepository.findById(id);
        if (sprint.isPresent()) {
            Sprint current = sprint.get();
            current.setName(updated.getName());
            current.setStartDate(updated.getStartDate());
            current.setEndDate(updated.getEndDate());
            current.setCreatedAt(updated.getCreatedAt());
            return sprintRepository.save(current);
        }
        return null;
    }

    /** Elimina el sprint con el ID especificado de la base de datos, devolviendo true si la eliminación fue exitosa o false si ocurrió un error. */
    public boolean delete(Long id) {
        try {
            sprintRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}