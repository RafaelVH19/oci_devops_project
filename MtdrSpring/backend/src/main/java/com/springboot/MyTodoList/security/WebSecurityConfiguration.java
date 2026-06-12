package com.springboot.MyTodoList.security;

import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.net.URL;
import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    @Value("${auth.server.url:http://auth-server:3001}")
    private String authServerUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            OracleRoleJwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
                config.setAllowCredentials(true);
                return config;
            }))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Error pages and SPA forwards (forward:/index.html) happen on
                // non-REQUEST dispatches; they must not re-trigger authorization.
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // SPA routes served as index.html (see SpaForwardController) + static assets
                .requestMatchers("/", "/landing", "/login", "/app", "/dashboard", "/dashboard/**",
                        "/lumi", "/manager", "/index.html", "/assets/**", "/static/**",
                        "/favicon.ico", "/manifest.json", "/*.js", "/*.css", "/*.png", "/*.svg").permitAll()
                // Auth server endpoints (JWKS, token, sign-in) and liveness probe
                .requestMatchers("/api/auth/**", "/health").permitAll()
                // API docs
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                // User management is manager-only
                .requestMatchers("/api/users/invite").hasRole("MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/users").hasRole("MANAGER")
                // Everything else requires a valid Better Auth JWT
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter))
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        try {
            URL jwksUrl = new URL(authServerUrl + "/api/auth/jwks");
            // Custom decoder: Nimbus' DefaultJWTProcessor cannot verify OKP/Ed25519 keys
            // (OctetKeyPair has no java.security.Key representation). See EdDSAJwtDecoder.
            return new EdDSAJwtDecoder(new RemoteJWKSet<SecurityContext>(jwksUrl));
        } catch (Exception e) {
            throw new RuntimeException("Failed creating JWT decoder", e);
        }
    }
}
