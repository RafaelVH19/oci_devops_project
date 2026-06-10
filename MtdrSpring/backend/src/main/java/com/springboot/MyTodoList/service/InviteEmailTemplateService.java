package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.InviteEmailContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Servicio encargado de renderizar las plantillas de correo electrónico de invitación.
 *
 * Proporciona métodos para generar el contenido HTML y de texto plano de los correos electrónicos
 * de invitación utilizando una plantilla predefinida y los datos proporcionados en el contexto de la invitación.
 */
@Service
public class InviteEmailTemplateService {

    private static final String HTML_TEMPLATE_PATH = "templates/email/invite.html";

    /** Renderiza el contenido HTML del correo electrónico de invitación utilizando la plantilla y los datos del contexto. */
    public String renderHtml(InviteEmailContext context) throws IOException {
        String template = StreamUtils.copyToString(
                new ClassPathResource(HTML_TEMPLATE_PATH).getInputStream(),
                StandardCharsets.UTF_8);

        String teamName = blankToDefault(context.getTeamName(), "your new team");
        String inviterName = blankToDefault(context.getInviterName(), "A team manager");
        String inviterEmail = blankToDefault(context.getInviterEmail(), "—");
        String roleLabel = formatRole(context.getRole());

        return template
                .replace("{{inviteeName}}", escape(context.getInviteeName()))
                .replace("{{inviteeEmail}}", escape(context.getInviteeEmail()))
                .replace("{{temporaryPassword}}", escape(context.getTemporaryPassword()))
                .replace("{{inviterName}}", escape(inviterName))
                .replace("{{inviterEmail}}", escape(inviterEmail))
                .replace("{{teamName}}", escape(teamName))
                .replace("{{teamLabel}}", escape(teamLabel(teamName)))
                .replace("{{roleLabel}}", escape(roleLabel))
                .replace("{{loginUrl}}", escape(context.getLoginUrl()));
    }

    /** Renderiza el contenido de texto plano del correo electrónico de invitación utilizando los datos del contexto. */
    public String renderPlainText(InviteEmailContext context) {
        String teamName = blankToDefault(context.getTeamName(), "your new team");
        String inviterName = blankToDefault(context.getInviterName(), "A team manager");
        String inviterEmail = blankToDefault(context.getInviterEmail(), "");
        String roleLabel = formatRole(context.getRole());

        StringBuilder body = new StringBuilder();
        body.append("Hi ").append(context.getInviteeName()).append(",\n\n");
        body.append(inviterName).append(" invited you to join ").append(teamName);
        body.append(" on Lumen as ").append(roleLabel).append(".\n\n");
        body.append("Invitation details\n");
        body.append("- Invited by: ").append(inviterName);
        if (!inviterEmail.isBlank()) {
            body.append(" (").append(inviterEmail).append(")");
        }
        body.append("\n- Team: ").append(teamName).append("\n");
        body.append("- Role: ").append(roleLabel).append("\n\n");
        body.append("Sign in: ").append(context.getLoginUrl()).append("\n\n");
        body.append("Email: ").append(context.getInviteeEmail()).append("\n");
        body.append("Temporary password: ").append(context.getTemporaryPassword()).append("\n\n");
        body.append("— Lumen Team\n");
        return body.toString();
    }

    /** Métodos auxiliares para formatear y escapar valores utilizados en las plantillas de correo electrónico. */
    private static String teamLabel(String teamName) {
        if (teamName == null || teamName.isBlank()) {
            return "a team on Lumen";
        }
        return "the team \"" + teamName + "\"";
    }

    /** Formatea el rol para que sea más legible en el correo electrónico, devolviendo "Manager" o "Developer" para los roles conocidos, y capitalizando el primer carácter para otros roles. */
    private static String formatRole(String role) {
        if (role == null || role.isBlank()) {
            return "Developer";
        }
        return switch (role.toUpperCase()) {
            case "MANAGER" -> "Manager";
            case "DEVELOPER" -> "Developer";
            default -> role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
        };
    }

    /** Devuelve el valor dado o un valor predeterminado si el valor es nulo o está en blanco, asegurándose de que el resultado no sea nulo ni contenga solo espacios en blanco. */
    private static String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    /** Escapa el valor dado para evitar problemas de inyección de HTML en las plantillas de correo electrónico, utilizando HtmlUtils de Spring para realizar el escape. */
    private static String escape(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }
}
