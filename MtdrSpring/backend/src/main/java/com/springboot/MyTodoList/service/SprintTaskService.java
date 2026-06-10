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

@Service
public class SprintTaskService {

    @Autowired
    private SprintTaskRepository sprintTaskRepository;

    public List<SprintTask> findAll() {
        return sprintTaskRepository.findAll();
    }

    public ResponseEntity<SprintTask> getById(SprintTaskId id) {
        Optional<SprintTask> sprintTask = sprintTaskRepository.findById(id);
        return sprintTask.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public SprintTask add(SprintTask sprintTask) {
        return sprintTaskRepository.save(sprintTask);
    }

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

    public boolean delete(SprintTaskId id) {
        try {
            sprintTaskRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}