package com.neuro_bank.module.user.service;

import com.neuro_bank.module.user.dto.request.RegisterRequest;
import com.neuro_bank.module.user.dto.request.UpdateProfileRequest;
import com.neuro_bank.module.user.dto.request.VerifyOtpRequest;
import com.neuro_bank.module.user.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {
  UserResponse register(RegisterRequest request);

  void verifyEmail(VerifyOtpRequest request);

  void resendOtp(String email);

  UserResponse getById(UUID id);

  UserResponse updateProfile(UUID id, UpdateProfileRequest request);
}
