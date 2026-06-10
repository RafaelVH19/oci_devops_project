package com.springboot.MyTodoList.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Servicio encargado de actuar como puente entre la aplicación y un servidor
 * de autenticación externo.
 *
 * Permite crear usuarios en el servidor de autenticación cuando se registran
 * en la aplicación, asegurando que ambos sistemas estén sincronizados.
 */
@Service
public class AuthBridgeService {

    private static final Logger logger = LoggerFactory.getLogger(AuthBridgeService.class);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${auth.server.url:http://localhost:3001}")
    private String authServerUrl;

    @Value("${auth.server.invite-secret:}")
    private String inviteSecret;

    /** Constructor que inyecta el objeto ObjectMapper */
    public AuthBridgeService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Crea un usuario en el servidor de autenticación */
    public boolean createAuthUser(String email, String password, String name, Long oracleUserId) {
        if (inviteSecret == null || inviteSecret.isBlank()) {
            logger.warn("auth.server.invite-secret is not set; skipping Better Auth user creation");
            return false;
        }
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("email", email);
            body.put("password", password);
            body.put("name", name);
            if (oracleUserId != null) {
                body.put("oracleUserId", oracleUserId);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(authServerUrl.replaceAll("/$", "") + "/internal/users"))
                    .header("Content-Type", "application/json")
                    .header("x-invite-secret", inviteSecret)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return true;
            }
            logger.warn("Auth server returned {}: {}", response.statusCode(), response.body());
            return false;
        } catch (Exception e) {
            logger.warn("Could not reach auth server at {}: {}", authServerUrl, e.getMessage());
            return false;
        }
    }
}
