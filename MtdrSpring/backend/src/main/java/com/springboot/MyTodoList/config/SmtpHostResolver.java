package com.springboot.MyTodoList.config;

import java.util.Locale;
import java.util.Map;

/**
 * Picks SMTP host/port from the sender email domain (Gmail, Outlook, etc.).
 */
public final class SmtpHostResolver {

    private static final Map<String, SmtpPreset> BY_DOMAIN = Map.ofEntries(
            Map.entry("gmail.com", new SmtpPreset("smtp.gmail.com", 587, true, true, false)),
            Map.entry("googlemail.com", new SmtpPreset("smtp.gmail.com", 587, true, true, false)),
            Map.entry("outlook.com", new SmtpPreset("smtp.office365.com", 587, true, true, false)),
            Map.entry("hotmail.com", new SmtpPreset("smtp.office365.com", 587, true, true, false)),
            Map.entry("live.com", new SmtpPreset("smtp.office365.com", 587, true, true, false)),
            Map.entry("yahoo.com", new SmtpPreset("smtp.mail.yahoo.com", 587, true, true, false)),
            Map.entry("icloud.com", new SmtpPreset("smtp.mail.me.com", 587, true, true, false)),
            Map.entry("proton.me", new SmtpPreset("smtp.protonmail.ch", 587, true, true, false)),
            Map.entry("protonmail.com", new SmtpPreset("smtp.protonmail.ch", 587, true, true, false)),
            Map.entry("zoho.com", new SmtpPreset("smtp.zoho.com", 587, true, true, false))
    );

    private SmtpHostResolver() {
    }

    public static SmtpPreset resolve(String fromEmail) {
        String domain = domainFromEmail(fromEmail);
        if (domain != null && BY_DOMAIN.containsKey(domain)) {
            return BY_DOMAIN.get(domain);
        }
        return new SmtpPreset("smtp." + (domain != null ? domain : "localhost"), 587, true, true, false);
    }

    public static String domainFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return null;
        }
        String part = email.substring(email.lastIndexOf('@') + 1).trim().toLowerCase(Locale.ROOT);
        int angle = part.indexOf('>');
        if (angle > 0) {
            part = part.substring(0, angle);
        }
        return part;
    }

    public record SmtpPreset(String host, int port, boolean auth, boolean startTls, boolean ssl) {
    }
}
