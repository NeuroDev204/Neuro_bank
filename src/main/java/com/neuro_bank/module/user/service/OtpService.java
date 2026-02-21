package com.neuro_bank.module.user.service;

import com.neuro_bank.module.user.entity.User;

public interface OtpService {
  void sendOtp(User user, String type);

  void verifyOtp(User user, String type, String code);

  void invalidateOtp(User user, String type);
}
