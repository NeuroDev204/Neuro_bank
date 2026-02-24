package com.neuro_bank.module.auth.util;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@SuppressWarnings("null")
@Component
public class CookieUtil {
  @Value("${app.cookie.domain:localhost}")
  private String domain;
  @Value("${app.cookie.secure:false}")
  private boolean secure;

  public ResponseCookie createAccessTokenCookie(String token) {
    return ResponseCookie.from("access_token", token)
        .httpOnly(true)
        .secure(secure)
        .path("/")
        .maxAge(Duration.ofMinutes(15))
        .sameSite("Strict")
        .domain(domain)
        .build();
  }

  public ResponseCookie createRefreshTokenCokkie(String token) {
    return ResponseCookie.from("refresh_token", token)
        .httpOnly(true)
        .secure(secure)
        .path("/api/v1/auth/refresh")
        .maxAge(Duration.ofDays(7))
        .sameSite("Strict")
        .domain(domain)
        .build();
  }

  public ResponseCookie deleteAccessTokenCookie() {
    return ResponseCookie.from("access_token", "")
        .httpOnly(true)
        .secure(secure)
        .path("/")
        .maxAge(Duration.ZERO)
        .sameSite("Strict")
        .domain(domain)
        .build();
  }

  public ResponseCookie deleteRefreshTokenCookie() {
    return ResponseCookie.from("refresh_token", "")
        .httpOnly(true).secure(secure)
        .path("/api/v1/auth/refresh").maxAge(Duration.ZERO)
        .sameSite("Strict").domain(domain).build();
  }

  public Optional<String> extractFromCookie(HttpServletRequest request, String name) {
    if (request.getCookies() == null)
      return Optional.empty();
    return Arrays.stream(request.getCookies())
        .filter(c -> name.equals(c.getName()))
        .map(Cookie::getValue)
        .findFirst();
  }
}
