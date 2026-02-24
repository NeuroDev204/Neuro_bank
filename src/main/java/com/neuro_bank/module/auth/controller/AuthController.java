package com.neuro_bank.module.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neuro_bank.common.response.ApiResponse;
import com.neuro_bank.module.auth.dto.request.LoginRequest;
import com.neuro_bank.module.auth.service.AuthService;
import com.neuro_bank.module.auth.service.LoginResult;
import com.neuro_bank.module.auth.util.CookieUtil;
import com.neuro_bank.module.user.dto.request.RegisterRequest;
import com.neuro_bank.module.user.dto.request.VerifyOtpRequest;
import com.neuro_bank.module.user.dto.response.UserResponse;
import com.neuro_bank.module.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor

public class AuthController {
  private final AuthService authService;
  private final UserService userService;
  private final CookieUtil cookieUtil;

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Registration successful. Please verify your email", userService.register(request)));
  }

  @PostMapping("/verify-otp")
  public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
    userService.verifyEmail(request);
    return ResponseEntity.ok(ApiResponse.success("OTP sent", null));
  }

  @PostMapping("/resend-otp")
  public ResponseEntity<ApiResponse<Void>> resendOtp(@RequestParam @Email String email) {
    userService.resendOtp(email);
    return ResponseEntity.ok(ApiResponse.success("OTP sent", null));
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody LoginRequest request,
      HttpServletRequest httpServletRequest) {
    LoginResult result = authService.login(request, getClientIp(httpServletRequest),
        httpServletRequest.getHeader("User-Agent"), request.getDeviceFingerprint());
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(result.accessToken()).toString())
        .header(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCokkie(result.refreshToken()).toString())
        .body(ApiResponse.success(result.response()));
  }

  
  private String getClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank())
      return forwarded.split(",")[0].trim();
    return request.getRemoteAddr();
  }
}
