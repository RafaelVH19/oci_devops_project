package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.TeamMember;
import com.springboot.MyTodoList.model.TeamMemberId;
import com.springboot.MyTodoList.service.TeamMemberService;
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
public class TeamMemberController {

    @Autowired
    private TeamMemberService teamMemberService;

    @GetMapping(value = "/team-members")
    public List<TeamMember> getAllTeamMembers() {
        return teamMemberService.findAll();
    }

    @GetMapping(value = "/team-members/{teamId}/{userId}")
    public ResponseEntity<TeamMember> getTeamMemberById(@PathVariable Long teamId, @PathVariable Long userId) {
        TeamMemberId id = new TeamMemberId(teamId, userId);
        ResponseEntity<TeamMember> response = teamMemberService.getById(id);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
    }

    @PostMapping(value = "/team-members")
    public ResponseEntity<TeamMember> addTeamMember(@RequestBody TeamMember teamMember) {
        TeamMember dbTeamMember = teamMemberService.add(teamMember);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location", dbTeamMember.getTeamId() + "/" + dbTeamMember.getMemberUserId());
        responseHeaders.set("Access-Control-Expose-Headers", "location");

        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(responseHeaders)
                .body(dbTeamMember);
    }

    @PutMapping(value = "/team-members/{teamId}/{userId}")
    public ResponseEntity<TeamMember> updateTeamMember(@RequestBody TeamMember teamMember,
                                                        @PathVariable Long teamId,
                                                        @PathVariable Long userId) {
        TeamMemberId id = new TeamMemberId(teamId, userId);
        TeamMember dbTeamMember = teamMemberService.update(id, teamMember);
        if (dbTeamMember == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(dbTeamMember, HttpStatus.OK);
    }

    @DeleteMapping(value = "/team-members/{teamId}/{userId}")
    public ResponseEntity<Boolean> deleteTeamMember(@PathVariable Long teamId, @PathVariable Long userId) {
        TeamMemberId id = new TeamMemberId(teamId, userId);
        boolean deleted = teamMemberService.delete(id);
        if (!deleted) {
            return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}
