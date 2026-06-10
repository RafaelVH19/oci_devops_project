package com.springboot.MyTodoList.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración principal de seguridad para la aplicación.
 *
 * Esta clase define las reglas de autenticación y autorización
 * utilizadas por Spring Security, así como la integración del filtro
 * personalizado de validación JWT.
 *
 * La configuración actual utiliza un modelo stateless, donde
 * cada petición debe contener su propio token JWT y no se almacenan
 * sesiones en el servidor.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** Constructor que inyecta el filtro de autenticación JWT */
    public WebSecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    
    /** Configura la cadena de filtros de seguridad */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/health", "/invite-user").permitAll() // Public endpoints
                .requestMatchers("/api/me").authenticated() // Example protected endpoint
                .anyRequest().permitAll() // Keep others permitted for now while you migrate, or change to .authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}

