package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.SprintTask;
import com.springboot.MyTodoList.model.SprintTaskId;
import com.springboot.MyTodoList.repository.SprintTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/** Servicio encargado de gestionar la lógica de negocio relacionada con las tareas de sprint, incluyendo operaciones CRUD para las tareas de sprint almacenadas en la base de datos a través del SprintTaskRepository. */
@Service
public class SprintTaskService {

    @Autowired
    private SprintTaskRepository sprintTaskRepository;

    /** Devuelve una lista de todas las tareas de sprint almacenadas en la base de datos. */
    public List<SprintTask> findAll() {
        return sprintTaskRepository.findAll();
    }

    /** Devuelve un ResponseEntity que contiene la tarea de sprint con el ID especificado si existe, o un estado NOT_FOUND si no se encuentra. */
    public ResponseEntity<SprintTask> getById(SprintTaskId id) {
        Optional<SprintTask> sprintTask = sprintTaskRepository.findById(id);
        return sprintTask.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /** Agrega una nueva tarea de sprint a la base de datos y devuelve la tarea de sprint guardada, incluyendo su ID generado. */
    public SprintTask add(SprintTask sprintTask) {
        return sprintTaskRepository.save(sprintTask);
    }

    /** Actualiza una tarea de sprint existente en la base de datos y devuelve la tarea de sprint actualizada. */
    public SprintTask update(SprintTaskId id, SprintTask updated) {
        Optional<SprintTask> sprintTask = sprintTaskRepository.findById(id);
        if (sprintTask.isPresent()) {
            SprintTask current = sprintTask.get();
            current.setAddedAt(updated.getAddedAt());
            current.setRemovedAt(updated.getRemovedAt());
            return sprintTaskRepository.save(current);
        }
        return null;
    }

    /** Elimina la tarea de sprint con el ID especificado de la base de datos, devolviendo true si la eliminación fue exitosa o false si ocurrió un error. */
    public boolean delete(SprintTaskId id) {
        try {
            sprintTaskRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}