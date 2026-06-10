package com.springboot.MyTodoList.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

/**
 * Proxies /api/auth to the Better Auth server so the SPA works from Docker (:8080)
 * without a separate Vite dev proxy.
 */
@RestController
public class AuthProxyController {

    private static final Set<String> SKIP_REQUEST_HEADERS = Set.of(
            "host", "connection", "content-length", "transfer-encoding"
    );

    private static final Set<String> SKIP_RESPONSE_HEADERS = Set.of(
            "connection", "transfer-encoding", "content-encoding"
    );

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Value("${auth.server.url:http://localhost:3001}")
    private String authServerUrl;

    @RequestMapping("/api/auth/**")
    public ResponseEntity<byte[]> proxyAuth(
            HttpServletRequest request,
            @RequestBody(required = false) byte[] body) throws Exception {

        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String target = authServerUrl.replaceAll("/$", "") + uri;
        if (query != null && !query.isBlank()) {
            target += "?" + query;
        }

        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(target));

        if (method == HttpMethod.GET || method == HttpMethod.HEAD) {
            builder.method(method.name(), HttpRequest.BodyPublishers.noBody());
        } else {
            builder.method(
                    method.name(),
                    body != null && body.length > 0
                            ? HttpRequest.BodyPublishers.ofByteArray(body)
                            : HttpRequest.BodyPublishers.noBody());
        }

        Collections.list(request.getHeaderNames()).forEach(name -> {
            if (SKIP_REQUEST_HEADERS.contains(name.toLowerCase())) {
                return;
            }
            Collections.list(request.getHeaders(name))
                    .forEach(value -> builder.header(name, value));
        });

        final HttpResponse<byte[]> upstream;
        try {
            upstream = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException e) {
            String errorJson = String.format(
                    "{\"error\":\"Auth server unreachable at %s. Run: docker compose up -d\"}",
                    authServerUrl);
            return ResponseEntity.status(502)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(errorJson.getBytes(StandardCharsets.UTF_8));
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        upstream.headers().map().forEach((name, values) -> {
            if (SKIP_RESPONSE_HEADERS.contains(name.toLowerCase())) {
                return;
            }
            responseHeaders.addAll(name, values);
        });

        return new ResponseEntity<>(upstream.body(), responseHeaders, upstream.statusCode());
    }
}
