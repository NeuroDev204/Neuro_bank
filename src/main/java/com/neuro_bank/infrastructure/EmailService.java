package com.neuro_bank.infrastructure;

public interface EmailService {
  void sendOtp(String toEmail, String fullName, String otpCode, String type);

  void sendWelcome(String toEmail, String fullName);

}
