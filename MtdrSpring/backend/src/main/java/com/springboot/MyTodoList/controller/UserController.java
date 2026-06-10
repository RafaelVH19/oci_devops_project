package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.dto.InviteUserRequest;
import com.springboot.MyTodoList.dto.InviteUserResponse;
import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.service.UserInviteService;
import com.springboot.MyTodoList.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

// New Imports for Security
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


/**
 * Controlador REST encargado de administrar usuarios,
 * invitaciones y operaciones de autenticación.
 */
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserInviteService userInviteService;

    //@CrossOrigin

    /** Obtiene todos los usuarios */
    @GetMapping(value = "/users")
    public List<User> getAllUsers(){
        return userService.findAll();
    }

    /** Obtiene un usuario por su dirección de correo electrónico */
    @GetMapping(value = "/users/by-email")
    public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
        return userService.findByEmail(email)
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /** Genera una invitación para un nuevo usuario */
    @PostMapping(value = "/invite-user")
    public ResponseEntity<InviteUserResponse> inviteUser(
            @RequestBody InviteUserRequest request,
            HttpServletRequest httpRequest) {
        InviteUserResponse response = userInviteService.invite(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //@CrossOrigin

    /** Obtiene un usuario por su ID */
    @GetMapping(value = "/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id){
        try{
            ResponseEntity<User> responseEntity = userService.getUserById(id);
            return new ResponseEntity<User>(responseEntity.getBody(), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    //@CrossOrigin

    /** Agrega un nuevo usuario */
    @PostMapping(value = "/adduser")
    public ResponseEntity<User> addUser(@RequestBody User newUser) throws Exception{
        User dbUser = userService.addUser(newUser);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location",""+dbUser.getId());
        responseHeaders.set("Access-Control-Expose-Headers","location");

        return ResponseEntity.ok()
                .headers(responseHeaders).build();
    }
    
    //@CrossOrigin

    /** Actualiza un usuario */
    @PutMapping(value = "updateUser/{id}")
    public ResponseEntity<User> updateUser(@RequestBody User user, @PathVariable int id){
        try{
            User dbUser = userService.updateUser(id, user);
            
            return new ResponseEntity<>(dbUser,HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
    //@CrossOrigin

    /** Elimina un usuario */
    @DeleteMapping(value = "deleteUser/{id}")
    public ResponseEntity<Boolean> deleteUser(@PathVariable("id") int id){
        Boolean flag = false;
        try{
            flag = userService.deleteUser(id);
            return new ResponseEntity<>(flag, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(flag,HttpStatus.NOT_FOUND);
        }
    }

    /** Metodo auxiliar utilizado para pruebas unitarias */
    @GetMapping(value = "/unitTestAdd")
    public User test(){
        return userService.test();
    }

    // New Secured Endpoint

    /** Endpoint protegido que verifica la autenticación del usuario mediante Spring Security */
    @GetMapping("/api/secured/test")
    @PreAuthorize("isAuthenticated()") // This annotation protects the endpoint
    public ResponseEntity<String> getSecuredMessage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName(); // This will be the email from the JWT
        return ResponseEntity.ok("Hello, " + userEmail + "! You accessed a secured endpoint.");
    }

}
