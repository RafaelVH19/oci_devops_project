package com.springboot.MyTodoList.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.MyTodoList.config.SmtpHostResolver;
import com.springboot.MyTodoList.dto.InviteEmailContext;
import com.springboot.MyTodoList.dto.MailSendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Sends Lumen emails through a transactional provider (Resend API) or SMTP inferred from the from-address domain.
 */
@Service
public class LumenMailService {

    private static final Logger logger = LoggerFactory.getLogger(LumenMailService.class);

    @Autowired
    private InviteEmailTemplateService templateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${lumen.mail.provider:resend}")
    private String provider;

    @Value("${lumen.mail.resend-api-key:}")
    private String resendApiKey;

    @Value("${lumen.mail.from:Lumen <onboarding@resend.dev>}")
    private String fromAddress;

    @Value("${lumen.mail.smtp-password:}")
    private String smtpPassword;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public boolean isConfigured() {
        if ("resend".equalsIgnoreCase(provider)) {
            return resendApiKey != null && !resendApiKey.isBlank();
        }
        return smtpPassword != null && !smtpPassword.isBlank() && fromAddress != null && !fromAddress.isBlank();
    }

    public MailSendResult sendInvite(InviteEmailContext context) {
        if (!isConfigured()) {
            return MailSendResult.fail(
                    "RESEND_API_KEY not set. Add it to application-local.properties and rebuild Docker.");
        }
        try {
            String html = templateService.renderHtml(context);
            String plain = templateService.renderPlainText(context);
            String subject = buildSubject(context);

            if ("resend".equalsIgnoreCase(provider)) {
                return sendViaResend(context.getInviteeEmail(), subject, html, plain);
            }
            return sendViaSmtp(context.getInviteeEmail(), subject, html, plain);
        } catch (Exception e) {
            logger.error("Failed to send invite to {}: {}", context.getInviteeEmail(), e.getMessage());
            return MailSendResult.fail(e.getMessage());
        }
    }

    private MailSendResult sendViaResend(String to, String subject, String html, String plain) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("from", fromAddress);
        body.put("to", new String[] { to });
        body.put("subject", subject);
        body.put("html", html);
        body.put("text", plain);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.resend.com/emails"))
                .header("Authorization", "Bearer " + resendApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            logger.info("Resend: invite sent to {}", to);
            return MailSendResult.ok();
        }
        logger.warn("Resend error {}: {}", response.statusCode(), response.body());
        return MailSendResult.fail(parseResendError(response.body()));
    }

    private static String parseResendError(String body) {
        if (body != null && body.contains("API key is invalid")) {
            return "Resend API key is invalid. Create a new key at resend.com/api-keys.";
        }
        if (body != null && body.contains("only send testing emails to your own email")) {
            return "Resend test mode: you can only send to the email you used to sign up for Resend.";
        }
        return body != null && !body.isBlank() ? body : "Resend rejected the email.";
    }

    private MailSendResult sendViaSmtp(String to, String subject, String html, String plain) throws Exception {
        String emailOnly = extractEmail(fromAddress);
        SmtpHostResolver.SmtpPreset preset = SmtpHostResolver.resolve(emailOnly);

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(preset.host());
        sender.setPort(preset.port());
        sender.setUsername(emailOnly);
        sender.setPassword(smtpPassword);

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(preset.auth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(preset.startTls()));
        props.put("mail.smtp.ssl.enable", String.valueOf(preset.ssl()));

        var message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(plain, html);
        sender.send(message);
        logger.info("SMTP ({}): invite sent to {}", preset.host(), to);
        return MailSendResult.ok();
    }

    private static String extractEmail(String from) {
        if (from == null) {
            return "";
        }
        int start = from.indexOf('<');
        int end = from.indexOf('>');
        if (start >= 0 && end > start) {
            return from.substring(start + 1, end).trim();
        }
        return from.trim();
    }

    private static String buildSubject(InviteEmailContext context) {
        String team = context.getTeamName();
        if (team != null && !team.isBlank()) {
            return "You're invited to join " + team + " on Lumen";
        }
        return "You're invited to Lumen";
    }
}
