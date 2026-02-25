package com.neuro_bank.infrastructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;

  @Value("${app.mail.from}")
  private String fromEmail;
  @Value("${app.mail.from-name}")
  private String fromName;

  @Override
  @Async("emailExecutor") // dung async de khong block request
  public void sendOtp(String toEmail, String fullName, String otpCode, String type) {
    String subject = resolveSubject(type);
    String message = resolveMessage(type);
    Context context = new Context();
    context.setVariables(Map.of(
        "fullName", fullName,
        "otpCode", otpCode,
        "message", message,
        "expiryMinutes", 5));
    String html = templateEngine.process("email/otp", context);
    try {
      sendHtml(toEmail, subject, html);
    } catch (MessagingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void sendHtml(String toEmail, String subject, String htmlContetn) throws MessagingException {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail, fromName);
      helper.setTo(toEmail);
      helper.setSubject(subject);
      helper.setText(htmlContetn, true);
      mailSender.send(message);
    } catch (MessagingException | UnsupportedEncodingException exception) {
      log.error("Failed to send email to={} subject={}", toEmail, subject, exception);
    }
  }

  private String resolveSubject(String type) {
    return switch (type) {
      case "EMAIL_VERIFICATION" -> "[Neuro Bank] Verify your email address";
      case "NEW_DEVICE_LOGIN" -> "[Neuro Bank] New device login detected";
      case "RESET_PASSWORD" -> "[Neuro Bank] Reset your password";
      case "TRANSACTION" -> "[Neuro Bank] Transaction verification code";
      default -> "[Neuro Bank] Verification code";
    };
  }

  private String resolveMessage(String type) {
    return switch (type) {
      case "EMAIL_VERIFICATION" -> "Please use the code below to verify your email address";
      case "NEW_DEVICE_LOGIN" -> "A login attempts was made from a new device. Use this code to verify it's you";
      case "TRANSACTION" -> "Use this code to confirm your transaction.";
      default -> "Your verification code is below";
    };
  }

  @Override
  @Async("emailExecutor")
  public void sendWelcome(String toEmail, String fullName) {
    Context ctx = new Context();
    ctx.setVariables(Map.of("fullName", fullName));
    String html = templateEngine.process("email/welcome", ctx);
    try {
      sendHtml(toEmail, "Welcome to YourBank!", html);
    } catch (MessagingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
