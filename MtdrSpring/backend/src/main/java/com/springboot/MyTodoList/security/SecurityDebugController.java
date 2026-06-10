package com.springboot.MyTodoList.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class SecurityDebugController {

    @GetMapping("/debug/auth")
    public Map<String, Object> debugAuth(Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            response.put("authenticated", false);
            response.put("message", "No authentication found");
            return response;
        }

        response.put("authenticated", auth.isAuthenticated());
        response.put("principal", auth.getPrincipal());
        response.put("authorities", auth.getAuthorities());
        response.put("name", auth.getName());

        return response;
    }
}