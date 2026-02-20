package com.neuro_bank.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {
  private static final String ACCESS_TOKEN_COOKIE="access_token";
  private static final String REFRESH_TOKEN_COOKIE="refresh_token";

  @Value("${app.jwt.cookie.domain}")
  private String domain;
  @Value("${app.jwt.cookie.secure}")
  private boolean secure;

  public ResponseCookie createAccessTokenCookie(String token){
    return ResponseCookie.from(ACCESS_TOKEN_COOKIE, token)
        .httpOnly(true)
        .secure(secure)
        .path("/")
        .maxAge(Duration.ofMinutes(15))
        .sameSite("Strict")
        .domain(domain)
        .build();
  }
  public ResponseCookie createRefreshTokenCookie(String token){
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE,token)
        .httpOnly(true)
        .sameSite("Struct")
        .secure(secure)
        .path("/api/v1/auth/refresh")
        .maxAge(Duration.ofDays(7))
        .domain(domain)
        .build();
  }

  public ResponseCookie deleteAccessTokenCookie(){
    return ResponseCookie.from(ACCESS_TOKEN_COOKIE,"")
        .domain(domain)
        .httpOnly(true)
        .path("/")
        .maxAge(Duration.ZERO)
        .sameSite("Strict")
        .secure(secure)
        .build();
  }
  public ResponseCookie deleteRefreshTokenCookie() {
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
        .httpOnly(true)
        .secure(secure)
        .path("/api/v1/auth/refresh")
        .maxAge(Duration.ZERO)
        .sameSite("Strict")
        .domain(domain)
        .build();
  }


 // Đọc refresh token từ cookie trong request
  public Optional<String> extractRefreshToken(HttpServletRequest request) {
    if (request.getCookies() == null) return Optional.empty();
    return Arrays.stream(request.getCookies())
        .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
        .map(Cookie::getValue)
        .findFirst();
  }
}
