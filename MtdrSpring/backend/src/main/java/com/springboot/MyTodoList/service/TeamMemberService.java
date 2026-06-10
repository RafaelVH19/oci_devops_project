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

@Service
public class TeamMemberService {

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    public List<TeamMember> findAll() {
        return teamMemberRepository.findAll();
    }

    public ResponseEntity<TeamMember> getById(TeamMemberId id) {
        Optional<TeamMember> teamMember = teamMemberRepository.findById(id);
        return teamMember.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public TeamMember add(TeamMember teamMember) {
        return teamMemberRepository.save(teamMember);
    }

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

    public boolean delete(TeamMemberId id) {
        try {
            teamMemberRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}