package com.sews.reporting.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Properties;

/**
 * Section 5.6 : Envoi automatique des rapports par email
 *
 * IMPORTANT : la configuration SMTP peut venir de deux endroits :
 *  1. application.properties (spring.mail.*) - utilisee par defaut au demarrage
 *  2. Les parametres enregistres via la page "Parametres > SMTP" (table system_settings,
 *     categorie "smtp") - si l'administrateur a rempli ces champs, ils sont prioritaires.
 *
 * Sans cette lecture dynamique, changer la config SMTP depuis l'interface n'avait
 * aucun effet : le JavaMailSender restait celui construit une seule fois au demarrage
 * avec les valeurs (souvent des placeholders "CHANGE_ME") d'application.properties.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender defaultMailSender;
    private final SystemSettingsService settingsService;

    /**
     * Construit un JavaMailSender a partir des parametres enregistres en base.
     * Si aucun parametre SMTP n'a ete sauvegarde (host/username vides), on retombe
     * sur le mailSender par defaut configure via application.properties.
     */
    private JavaMailSender resolveMailSender() {
        String host = settingsService.getSetting("smtp.host");
        String username = settingsService.getSetting("smtp.username");
        String password = settingsService.getSetting("smtp.password");
        String portStr = settingsService.getSetting("smtp.port");

        boolean hasDbConfig = host != null && !host.isBlank()
                && username != null && !username.isBlank();

        if (!hasDbConfig) {
            log.debug("Aucune config SMTP en base, utilisation de application.properties");
            return defaultMailSender;
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(portStr != null && !portStr.isBlank() ? Integer.parseInt(portStr) : 587);
        sender.setUsername(username);
        sender.setPassword(password != null ? password : "");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return sender;
    }

    private String resolveFromAddress() {
        String from = settingsService.getSetting("smtp.from");
        return (from != null && !from.isBlank()) ? from : null;
    }

    public void sendReportByEmail(String[] recipients, String subject, String body, String attachmentPath) {
        try {
            JavaMailSender mailSender = resolveMailSender();
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(recipients);
            helper.setSubject(subject);
            helper.setText(body);
            String from = resolveFromAddress();
            if (from != null) {
                helper.setFrom(from);
            }

            if (attachmentPath != null) {
                File attachment = new File(attachmentPath);
                if (attachment.exists()) {
                    helper.addAttachment(attachment.getName(), attachment);
                }
            }

            mailSender.send(message);
            log.info("Rapport envoye par email a : {}", String.join(", ", recipients));
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email", e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email : " + e.getMessage(), e);
        } catch (org.springframework.mail.MailException e) {
            // Erreurs de connexion/auth SMTP (mauvais identifiants, host injoignable, etc.)
            log.error("Erreur SMTP lors de l'envoi de l'email", e);
            throw new RuntimeException(
                    "Erreur SMTP : impossible d'envoyer l'email. Verifiez la configuration SMTP "
                            + "(hote, port, identifiants) dans Parametres > SMTP. Detail : " + e.getMessage(), e);
        }
    }

    public void sendTestEmail(String recipient) {
        try {
            JavaMailSender mailSender = resolveMailSender();
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(recipient);
            helper.setSubject("Test de configuration SMTP - SEWS Reporting");
            helper.setText("Ceci est un email de test pour vérifier la configuration SMTP.\n\n" +
                    "Si vous recevez ce message, votre configuration email est correcte.\n\n" +
                    "Cordialement,\n" +
                    "SEWS Reporting System", false);
            String from = resolveFromAddress();
            if (from != null) {
                helper.setFrom(from);
            }

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de test : " + e.getMessage(), e);
        } catch (org.springframework.mail.MailException e) {
            throw new RuntimeException(
                    "Erreur SMTP : impossible d'envoyer l'email de test. Verifiez la configuration SMTP "
                            + "(hote, port, identifiants). Detail : " + e.getMessage(), e);
        }
    }
}
