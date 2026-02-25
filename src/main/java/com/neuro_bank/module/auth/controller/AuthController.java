package com.neuro_bank.module.auth.controller;

import com.neuro_bank.common.exception.BusinessException;
import com.neuro_bank.common.response.ApiResponse;
import com.neuro_bank.module.auth.dto.request.LoginRequest;
import com.neuro_bank.module.auth.dto.request.VerifyNewDeviceRequest;
import com.neuro_bank.module.auth.service.AuthService;
import com.neuro_bank.module.auth.service.LoginResult;
import com.neuro_bank.module.auth.service.RefreshResult;
import com.neuro_bank.module.auth.util.CookieUtil;
import com.neuro_bank.module.user.dto.request.RegisterRequest;
import com.neuro_bank.module.user.dto.request.UpdateProfileRequest;
import com.neuro_bank.module.user.dto.request.VerifyOtpRequest;
import com.neuro_bank.module.user.dto.response.UserResponse;
import com.neuro_bank.module.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    return ResponseEntity.ok(ApiResponse.success("OTP verify successfully. Please login", null));
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

  @PostMapping("/verify-new-device")
  public ResponseEntity<ApiResponse<UserResponse>> verifyNewDevice(
      @Valid @RequestBody VerifyNewDeviceRequest request, HttpServletRequest httpServletRequest) {
    LoginResult loginResult = authService.verifyNewDevice(request, getClientIp(httpServletRequest),
        httpServletRequest.getHeader("User-Agent"));
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(loginResult.accessToken()).toString())
        .header(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCokkie(loginResult.refreshToken()).toString())
        .body(ApiResponse.success(loginResult.response()));
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<Void>> refresh(HttpServletRequest request) {
    String refreshToken = cookieUtil.extractFromCookie(request, "refresh_token")
        .orElseThrow(() -> BusinessException.unauthorized("Refresh token not found"));
    String deviceFingerprint = request.getHeader("X-Device-Fingerprint");
    RefreshResult refreshResult = authService.refreshAccessToken(refreshToken, getClientIp(request), deviceFingerprint);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(refreshResult.newAccessToken()).toString())
        .header(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCokkie(refreshResult.newRefreshToken()).toString())
        .body(ApiResponse.success(null));
  }

  @PostMapping("/logout")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request,
                                                  @AuthenticationPrincipal com.neuro_bank.security.UserPrincipal principal) {
    authService.logout(
        cookieUtil.extractFromCookie(request, "refresh_token").orElse(null),
        cookieUtil.extractFromCookie(request, "access_token").orElse(null),
        principal.getId());
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookieUtil.deleteAccessTokenCookie().toString())
        .header(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshTokenCookie().toString())
        .body(ApiResponse.success("Logged out successfully", null));
  }

  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<UserResponse>> me(
      @AuthenticationPrincipal com.neuro_bank.security.UserPrincipal principal) {
    return ResponseEntity.ok(ApiResponse.success(userService.getById(principal.getId())));
  }

  @PatchMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
      @AuthenticationPrincipal com.neuro_bank.security.UserPrincipal principal,
      @Valid @RequestBody UpdateProfileRequest request) {
    return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(principal.getId(), request)));
  }

  private String getClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank())
      return forwarded.split(",")[0].trim();
    return request.getRemoteAddr();
  }
}
