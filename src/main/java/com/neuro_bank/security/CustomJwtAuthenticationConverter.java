package com.neuro_bank.security;

import com.neuro_bank.infrastructure.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomJwtAuthenticationConverter
    implements Converter<Jwt, AbstractAuthenticationToken> {

  private final CustomUserDetailsService userDetailsService;
  private final RedisService redisService;

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    // 1. Chỉ ACCESS token được dùng cho API
    String tokenType = jwt.getClaimAsString("tokenType");
    if (!"ACCESS".equals(tokenType))
      throw new BadCredentialsException("Invalid token type");

    // 2. Check blacklist — token đã logout
    String jti = jwt.getId();
    if (jti != null && redisService.isTokenBlacklisted(jti))
      throw new BadCredentialsException("Token has been revoked");

    // 3. Load user để lấy status mới nhất từ DB
    UUID userId = UUID.fromString(jwt.getSubject());
    UserDetails userDetails = userDetailsService.loadByUserId(userId);

    return new UsernamePasswordAuthenticationToken(
        userDetails, jwt, userDetails.getAuthorities());
  }
}