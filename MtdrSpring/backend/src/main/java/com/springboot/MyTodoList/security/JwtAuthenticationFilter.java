package com.springboot.MyTodoList.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * Filtro de autenticación basado en JWT utilizado por Spring Security.
 *
 * Esta clase intercepta cada petición HTTP entrante para verificar
 * si contiene un token JWT válido en la cabecera {@code Authorization}.
 * Cuando el token es válido, se extrae la información del usuario
 * autenticado y se registra dentro del contexto de seguridad de Spring.
 *
 * El filtro utiliza la misma clave secreta configurada en Better Auth
 * para validar tokens firmados mediante el algoritmo HS256.
 *
 * Si el token es inválido, expiró o no puede ser verificado,
 * el contexto de seguridad es limpiado y la petición continúa sin
 * autenticación.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** La clave secreta utilizada para validar los tokens JWT */
    @Value("${better-auth.secret}")
    private String secret;

    /** Ejecuta la validacion del token JWT para cada solicitud HTTP */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // Better Auth uses the secret directly to sign JWTs (HS256)
                SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String email = claims.get("email", String.class);
                // We can also extract custom fields like oracleUserId if added to JWT
                
                if (email != null) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            email, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                // Invalid token - clear context
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
