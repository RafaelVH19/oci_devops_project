package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.config.AppUrlResolver;
import com.springboot.MyTodoList.dto.InviteEmailContext;
import com.springboot.MyTodoList.dto.InviteUserRequest;
import com.springboot.MyTodoList.dto.InviteUserResponse;
import com.springboot.MyTodoList.dto.MailSendResult;
import com.springboot.MyTodoList.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Optional;

@Service
public class UserInviteService {

    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final int PASSWORD_LENGTH = 12;

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private UserService userService;

    @Autowired
    private AuthBridgeService authBridgeService;

    @Autowired
    private InviteEmailService inviteEmailService;

    @Autowired
    private AppUrlResolver appUrlResolver;

    public InviteUserResponse invite(InviteUserRequest request, HttpServletRequest httpRequest) {
        String name = trimRequired(request.getName(), "name");
        String email = trimRequired(request.getEmail(), "email").toLowerCase();

        Optional<User> existing = userService.findByEmail(email);
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A user with this email already exists");
        }

        String temporaryPassword = generateTemporaryPassword();
        String loginUrl = appUrlResolver.resolveLoginUrl(httpRequest, request.getAppOrigin());

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setTelegramId(email.split("@")[0]);
        user.setRole(request.getRole() != null ? request.getRole() : "DEVELOPER");
        user.setWorkMode(request.getWorkMode() != null ? request.getWorkMode() : "REMOTE");
        user.setIsActive(1);
        user.setPasswordHash(temporaryPassword);

        User saved = userService.addUser(user);
        boolean authCreated = authBridgeService.createAuthUser(
                email,
                temporaryPassword,
                name,
                saved.getId());

        InviteEmailContext emailContext = new InviteEmailContext(
                name,
                email,
                temporaryPassword,
                request.getInvitedByName(),
                request.getInvitedByEmail(),
                request.getTeamName(),
                user.getRole(),
                loginUrl);
        MailSendResult mailResult = inviteEmailService.sendInviteEmail(emailContext);

        return new InviteUserResponse(
                saved,
                temporaryPassword,
                authCreated,
                mailResult.isSent(),
                mailResult.getErrorMessage());
    }

    private static String trimRequired(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return value.trim();
    }

    private String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(PASSWORD_CHARS.charAt(secureRandom.nextInt(PASSWORD_CHARS.length())));
        }
        return password.toString();
    }
}
