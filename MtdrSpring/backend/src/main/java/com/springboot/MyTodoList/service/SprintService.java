package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.repository.SprintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SprintService {

    @Autowired
    private SprintRepository sprintRepository;

    public List<Sprint> findAll() {
        return sprintRepository.findAll();
    }

    public ResponseEntity<Sprint> getById(Long id) {
        Optional<Sprint> sprint = sprintRepository.findById(id);
        return sprint.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public Sprint add(Sprint sprint) {
        return sprintRepository.save(sprint);
    }

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

    public boolean delete(Long id) {
        try {
            sprintRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}