package com.satwik.splitora.service.implementations;

import com.satwik.splitora.persistence.entities.User;
import com.satwik.splitora.persistence.entities.UserEvents;
import com.satwik.splitora.repository.UserEventsRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
public class NotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final UserEventsRepository userEventsRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public NotificationService(JavaMailSender mailSender, TemplateEngine templateEngine, UserEventsRepository userEventsRepository) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.userEventsRepository = userEventsRepository;
    }

    @Async
    public void sendWelcomeEmail(User user) {
        String email = user.getEmail();
        if (email == null || email.isEmpty()) {
            log.warn("User {} does not have a valid email address. Skipping welcome email.", user.getUsername());
            return;
        }

        UserEvents userEvents = user.getUserEvents();
        if (userEvents == null) {
            log.warn("UserEvents not found for user: {}. Skipping welcome email.", user.getEmail());
            return;
        }

        if (userEvents.isWelcomeEmailSent()) {
            log.info("Welcome email already sent to {}. Skipping.", user.getEmail());
            return;
        }

        // set welcome email sent to true before sending email to avoid duplicate emails in case of retries
        userEvents.setWelcomeEmailSent(true);
        userEventsRepository.save(userEvents);
        log.info("User events updated for user: {}, welcome email set to true.", user.getEmail());

        try {
            Context context = new Context();
            context.setVariable("username", user.getUsername());

            String body = templateEngine.process("welcome-message", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Welcome to Splitora!");
            helper.setText(body, true);

            mailSender.send(mimeMessage);

            log.info("Welcome email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}", email, e);

            userEvents.setWelcomeEmailSent(false);
            userEventsRepository.save(userEvents);
            log.info("Rollback welcome email sent value to false, as email is not sent for user, {}.", user.getEmail());
        }
    }
}
