package com.neuro_bank.module.auth.service;

import com.neuro_bank.module.auth.dto.request.LoginRequest;
import com.neuro_bank.module.auth.dto.request.VerifyNewDeviceRequest;

import java.util.UUID;

public interface AuthService {
  LoginResult login(LoginRequest request, String ipAddress, String userAgent, String deviceFingerprint);

  LoginResult verifyNewDevice(VerifyNewDeviceRequest request, String ipAddress, String userAgent);

  RefreshResult refreshAccessToken(String refreshToken, String ipAddress, String deviceFingerprint);

  void logout(String refreshToken, String accessToken, UUID userId);
}
