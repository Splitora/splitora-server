package com.satwik.splitora.service;

import com.satwik.splitora.persistence.entities.User;
import com.satwik.splitora.persistence.entities.UserEvents;
import com.satwik.splitora.repository.UserEventsRepository;
import com.satwik.splitora.service.implementations.NotificationService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private UserEventsRepository userEventsRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        // mimic the injected property value
        notificationService = new NotificationService(mailSender, templateEngine, userEventsRepository);
        // manually inject value since @Value doesn’t work in plain unit test
        var fromEmailField = "fromEmail";
        try {
            var field = NotificationService.class.getDeclaredField(fromEmailField);
            field.setAccessible(true);
            field.set(notificationService, "noreply@splitora.com");
        } catch (Exception ignored) {
            // ignoring exception as this is just for test setup
        }
    }

    @Test
    void testSendWelcomeEmail_sendsSuccessfully() {
        // Arrange
        UserEvents userEvents = new UserEvents();
        userEvents.setWelcomeEmailSent(false);

        User user = new User();
        user.setEmail("testUser@example.com");
        user.setUsername("Test User");
        user.setUserEvents(userEvents);

        MimeMessage mimeMessage = mock(MimeMessage.class);
        doReturn(mimeMessage).when(mailSender).createMimeMessage();

        // Avoid strict argument issues
        doReturn("<html>Welcome!</html>")
                .when(templateEngine)
                .process(eq("welcome-message"), any(Context.class));

        // Act
        notificationService.sendWelcomeEmail(user);

        // Assert
        verify(userEventsRepository, atLeastOnce()).save(userEvents);
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void testSendWelcomeEmail_whenEmailIsNull_shouldSkip() {
        User user = new User();
        user.setEmail(null);
        user.setUsername("NoEmailUser");

        notificationService.sendWelcomeEmail(user);

        verifyNoInteractions(userEventsRepository, mailSender, templateEngine);
    }

    @Test
    void testSendWelcomeEmail_whenUserEventsNull_shouldSkip() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setUserEvents(null);

        notificationService.sendWelcomeEmail(user);

        verifyNoInteractions(mailSender, templateEngine);
    }

    @Test
    void testSendWelcomeEmail_whenAlreadySent_shouldSkip() {
        UserEvents userEvents = new UserEvents();
        userEvents.setWelcomeEmailSent(true);

        User user = new User();
        user.setEmail("already@example.com");
        user.setUserEvents(userEvents);

        notificationService.sendWelcomeEmail(user);

        verify(userEventsRepository, never()).save(any());
        verifyNoInteractions(mailSender, templateEngine);
    }

    @Test
    void testSendWelcomeEmail_whenExceptionOccurs_shouldRollback() {
        // Arrange
        UserEvents userEvents = new UserEvents();
        userEvents.setWelcomeEmailSent(false);

        User user = new User();
        user.setEmail("fail@example.com");
        user.setUsername("TestFail");
        user.setUserEvents(userEvents);

        MimeMessage mimeMessage = mock(MimeMessage.class);
        doReturn(mimeMessage).when(mailSender).createMimeMessage();

        doReturn("<html>Welcome!</html>")
                .when(templateEngine)
                .process(eq("welcome-message"), any(Context.class));

        // Force mailSender.send() to throw an exception
        doThrow(new RuntimeException("Mail error")).when(mailSender).send(any(MimeMessage.class));

        // Act
        notificationService.sendWelcomeEmail(user);

        // Assert
        verify(userEventsRepository, atLeast(2)).save(userEvents); // one before, one after failure
        verify(mailSender).send(mimeMessage);
    }
}
