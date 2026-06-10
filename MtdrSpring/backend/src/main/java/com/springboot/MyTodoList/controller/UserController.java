package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.dto.InviteUserRequest;
import com.springboot.MyTodoList.dto.InviteUserResponse;
import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.service.UserInviteService;
import com.springboot.MyTodoList.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final UserInviteService userInviteService;

    public UserController(
            UserService userService,
            UserInviteService userInviteService) {
        this.userService = userService;
        this.userInviteService = userInviteService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/users/by-email")
    public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/users")
    public ResponseEntity<User> addUser(
            @Valid @RequestBody User newUser) {

        User savedUser = userService.addUser(newUser);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedUser);
    }

    @PostMapping("/users/invite")
    public ResponseEntity<InviteUserResponse> inviteUser(
            @Valid @RequestBody InviteUserRequest request,
            HttpServletRequest httpRequest) {

        InviteUserResponse response =
                userInviteService.invite(request, httpRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}