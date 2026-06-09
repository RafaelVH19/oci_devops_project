package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.SprintTask;
import com.springboot.MyTodoList.model.SprintTaskId;
import com.springboot.MyTodoList.service.SprintTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador encargado de administrar la relación
 * entre tareas y sprints.
 */
@RestController
public class SprintTaskController {

    @Autowired
    private SprintTaskService sprintTaskService;
    
    /** Obtiene todas las relaciones sprint-tarea registradas */
    @GetMapping(value = "/sprint-tasks")
    public List<SprintTask> getAllSprintTasks() {
        return sprintTaskService.findAll();
    }

    /** Obtiene una relación sprint-tarea específica mediante sus IDs */
    @GetMapping(value = "/sprint-tasks/{sprintId}/{taskId}")
    public ResponseEntity<SprintTask> getSprintTaskById(@PathVariable Long sprintId, @PathVariable Long taskId) {
        SprintTaskId id = new SprintTaskId(sprintId, taskId);
        ResponseEntity<SprintTask> response = sprintTaskService.getById(id);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
    }

    /** Registra una nueva relación sprint-tarea */
    @PostMapping(value = "/sprint-tasks")
    public ResponseEntity<SprintTask> addSprintTask(@RequestBody SprintTask sprintTask) {
        SprintTask dbSprintTask = sprintTaskService.add(sprintTask);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location", dbSprintTask.getSprintId() + "/" + dbSprintTask.getTaskId());
        responseHeaders.set("Access-Control-Expose-Headers", "location");

        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(responseHeaders)
                .body(dbSprintTask);
    }

    /** Actualiza la información de una relación sprint-tarea existente */
    @PutMapping(value = "/sprint-tasks/{sprintId}/{taskId}")
    public ResponseEntity<SprintTask> updateSprintTask(@RequestBody SprintTask sprintTask,
                                                        @PathVariable Long sprintId,
                                                        @PathVariable Long taskId) {
        SprintTaskId id = new SprintTaskId(sprintId, taskId);
        SprintTask dbSprintTask = sprintTaskService.update(id, sprintTask);
        if (dbSprintTask == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(dbSprintTask, HttpStatus.OK);
    }

    /** Elimina una relación sprint-tarea existente */
    @DeleteMapping(value = "/sprint-tasks/{sprintId}/{taskId}")
    public ResponseEntity<Boolean> deleteSprintTask(@PathVariable Long sprintId, @PathVariable Long taskId) {
        SprintTaskId id = new SprintTaskId(sprintId, taskId);
        boolean deleted = sprintTaskService.delete(id);
        if (!deleted) {
            return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}
