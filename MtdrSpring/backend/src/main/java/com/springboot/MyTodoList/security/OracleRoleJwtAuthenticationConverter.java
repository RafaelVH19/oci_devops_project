package com.springboot.MyTodoList.security;

import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.service.UserContextService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Maps a verified Better Auth JWT to application authorities.
 *
 * The role comes from the Oracle USERS table (MANAGER / DEVELOPER), not from the
 * token's "role" claim — Better Auth always sets that to its own internal value
 * ("user"), and Oracle is the single source of truth for identity (see
 * UserContextService). Unknown or inactive users are rejected as invalid tokens,
 * which Spring Security translates to a 401.
 */
@Component
public class OracleRoleJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserContextService userContextService;

    public OracleRoleJwtAuthenticationConverter(UserContextService userContextService) {
        this.userContextService = userContextService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new InvalidBearerTokenException("JWT does not contain an email claim");
        }

        User user;
        try {
            user = userContextService.loadByEmail(email);
        } catch (Exception e) {
            throw new InvalidBearerTokenException("No active user for " + email, e);
        }

        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase(Locale.ROOT)));

        return new JwtAuthenticationToken(jwt, authorities, email);
    }
}
