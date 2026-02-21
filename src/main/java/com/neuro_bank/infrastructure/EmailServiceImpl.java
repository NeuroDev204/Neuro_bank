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

import java.io.UnsupportedEncodingException;

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
  public void sendOtp(String toEmail, String fullName, String otpCode) {

  }

  private void sendHtml(String toEmail, String subject, String htmlContetn) throws MessagingException {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail, fromName);
      helper.setTo(toEmail);
      helper.setSubject(subject);
      helper.setText(htmlContetn, true);
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
  public void sendWelcome(String toEmail, String fullName) {

  }
}
