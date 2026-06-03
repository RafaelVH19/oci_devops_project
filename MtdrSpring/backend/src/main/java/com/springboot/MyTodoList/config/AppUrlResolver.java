package com.springboot.MyTodoList.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * Resolves the public app base URL (localhost vs production) for links in emails.
 */
@Component
public class AppUrlResolver {

    @Value("${lumen.public-url:}")
    private String configuredPublicUrl;

    public String resolveBaseUrl(HttpServletRequest request, String clientOrigin) {
        if (configuredPublicUrl != null && !configuredPublicUrl.isBlank()) {
            return trimTrailingSlash(configuredPublicUrl);
        }
        if (clientOrigin != null && !clientOrigin.isBlank()) {
            return trimTrailingSlash(clientOrigin);
        }
        if (request != null) {
            String fromOrigin = request.getHeader("Origin");
            if (fromOrigin != null && !fromOrigin.isBlank()) {
                return trimTrailingSlash(fromOrigin);
            }
            String referer = request.getHeader("Referer");
            String fromReferer = originFromReferer(referer);
            if (fromReferer != null) {
                return fromReferer;
            }
            String forwarded = forwardedBaseUrl(request);
            if (forwarded != null) {
                return forwarded;
            }
        }
        return "http://localhost:8080";
    }

    public String resolveLoginUrl(HttpServletRequest request, String clientOrigin) {
        return resolveBaseUrl(request, clientOrigin) + "/login";
    }

    private static String forwardedBaseUrl(HttpServletRequest request) {
        String host = request.getHeader("X-Forwarded-Host");
        if (host == null || host.isBlank()) {
            host = request.getHeader("Host");
        }
        if (host == null || host.isBlank()) {
            return null;
        }
        String proto = request.getHeader("X-Forwarded-Proto");
        if (proto == null || proto.isBlank()) {
            proto = request.getScheme();
        }
        return trimTrailingSlash(proto + "://" + host.split(",")[0].trim());
    }

    private static String originFromReferer(String referer) {
        if (referer == null || referer.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(referer.trim());
            if (uri.getScheme() == null || uri.getHost() == null) {
                return null;
            }
            int port = uri.getPort();
            if (port > 0 && port != 80 && port != 443) {
                return uri.getScheme() + "://" + uri.getHost() + ":" + port;
            }
            return uri.getScheme() + "://" + uri.getHost();
        } catch (Exception e) {
            return null;
        }
    }

    private static String trimTrailingSlash(String url) {
        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
