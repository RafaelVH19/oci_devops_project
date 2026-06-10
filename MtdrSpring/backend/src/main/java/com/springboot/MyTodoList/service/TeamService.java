package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Team;
import com.springboot.MyTodoList.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    public List<Team> findAll() {
        return teamRepository.findAll();
    }

    public ResponseEntity<Team> getById(Long id) {
        Optional<Team> team = teamRepository.findById(id);
        return team.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public Team add(Team team) {
        return teamRepository.save(team);
    }

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

    public boolean delete(Long id) {
        try {
            teamRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}