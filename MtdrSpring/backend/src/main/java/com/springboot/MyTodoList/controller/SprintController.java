package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.service.SprintService;
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

@RestController
public class SprintController {

    @Autowired
    private SprintService sprintService;

    @GetMapping(value = "/sprints")
    public List<Sprint> getAllSprints() {
        return sprintService.findAll();
    }

    @GetMapping(value = "/sprints/{id}")
    public ResponseEntity<Sprint> getSprintById(@PathVariable Long id) {
        ResponseEntity<Sprint> response = sprintService.getById(id);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
    }

    @PostMapping(value = "/sprints")
    public ResponseEntity<Sprint> addSprint(@RequestBody Sprint sprint) {
        Sprint dbSprint = sprintService.add(sprint);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location", "" + dbSprint.getId());
        responseHeaders.set("Access-Control-Expose-Headers", "location");

        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(responseHeaders)
                .body(dbSprint);
    }

    @PutMapping(value = "/sprints/{id}")
    public ResponseEntity<Sprint> updateSprint(@RequestBody Sprint sprint, @PathVariable Long id) {
        Sprint dbSprint = sprintService.update(id, sprint);
        if (dbSprint == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(dbSprint, HttpStatus.OK);
    }

    @DeleteMapping(value = "/sprints/{id}")
    public ResponseEntity<Boolean> deleteSprint(@PathVariable("id") Long id) {
        boolean deleted = sprintService.delete(id);
        if (!deleted) {
            return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}
