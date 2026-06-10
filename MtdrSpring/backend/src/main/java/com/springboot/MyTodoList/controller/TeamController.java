package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.controller.dto.TeamWithUsersResponse;
import com.springboot.MyTodoList.controller.dto.UserSummaryResponse;
import com.springboot.MyTodoList.model.Team;
import com.springboot.MyTodoList.model.TeamMember;
import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.repository.TeamMemberRepository;
import com.springboot.MyTodoList.repository.TeamRepository;
import com.springboot.MyTodoList.repository.UserRepository;
import com.springboot.MyTodoList.service.TeamService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping(value = "/teams")
    public List<TeamWithUsersResponse> getAllTeams() {
        List<Team> teams = teamService.findAll();
        List<TeamWithUsersResponse> response = new ArrayList<>();
        for (Team team : teams) {
            response.add(buildTeamWithUsers(team));
        }
        return response;
    }

    @GetMapping(value = "/teams/{id}")
    public ResponseEntity<TeamWithUsersResponse> getTeamById(@PathVariable Long id) {
        ResponseEntity<Team> response = teamService.getById(id);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(buildTeamWithUsers(response.getBody()), HttpStatus.OK);
    }

    @GetMapping(value = "/teams/{teamId}/users")
    public ResponseEntity<List<UserSummaryResponse>> getUsersByTeam(@PathVariable Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<UserSummaryResponse> users = findUsersByTeamId(teamId);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping(value = "/users/{userId}/teams")
    public ResponseEntity<List<TeamWithUsersResponse>> getTeamsByUser(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<TeamMember> teamMembers = teamMemberRepository.findByIdMemberUserId(userId);
        List<TeamWithUsersResponse> teams = new ArrayList<>();

        for (TeamMember teamMember : teamMembers) {
            if (teamMember.getTeamId() == null) {
                continue;
            }

            Optional<Team> team = teamRepository.findById(teamMember.getTeamId());
            if (team.isPresent()) {
                teams.add(buildTeamWithUsers(team.get()));
            }
        }

        return new ResponseEntity<>(teams, HttpStatus.OK);
    }

    @PostMapping(value = "/teams")
    public ResponseEntity<TeamWithUsersResponse> addTeam(@RequestBody Team team) {
        Team dbTeam = teamService.add(team);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location", "" + dbTeam.getId());
        responseHeaders.set("Access-Control-Expose-Headers", "location");

        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(responseHeaders)
                .body(buildTeamWithUsers(dbTeam));
    }

    @PutMapping(value = "/teams/{id}")
    public ResponseEntity<TeamWithUsersResponse> updateTeam(@RequestBody Team team, @PathVariable Long id) {
        Team dbTeam = teamService.update(id, team);
        if (dbTeam == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(buildTeamWithUsers(dbTeam), HttpStatus.OK);
    }

    @DeleteMapping(value = "/teams/{id}")
    public ResponseEntity<Boolean> deleteTeam(@PathVariable("id") Long id) {
        boolean deleted = teamService.delete(id);
        if (!deleted) {
            return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    private TeamWithUsersResponse buildTeamWithUsers(Team team) {
        List<UserSummaryResponse> users = findUsersByTeamId(team.getId());
        return new TeamWithUsersResponse(team, users);
    }

    private List<UserSummaryResponse> findUsersByTeamId(Long teamId) {
        List<TeamMember> teamMembers = teamMemberRepository.findByIdTeamId(teamId);
        List<UserSummaryResponse> users = new ArrayList<>();

        for (TeamMember teamMember : teamMembers) {
            if (teamMember.getMemberUserId() == null) {
                continue;
            }

            Optional<User> user = userRepository.findById(teamMember.getMemberUserId());
            user.ifPresent(value -> users.add(new UserSummaryResponse(value)));
        }

        return users;
    }
}
