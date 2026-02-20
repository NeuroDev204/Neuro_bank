package com.neuro_bank.module.auth.service;

import com.neuro_bank.module.auth.dto.request.LoginRequest;
import com.neuro_bank.module.auth.dto.response.LoginResponse;

import java.util.UUID;

public interface AuthService {
   LoginResponse login(LoginRequest request,String ipAddress,String userAgent);
   LoginResponse refreshToken(String refreshToken,String ipAddress);
   void logout(String refreshToken, UUID userId);
}
