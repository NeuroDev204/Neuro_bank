package com.neuro_bank.security;

import com.neuro_bank.common.exception.BusinessException;
import com.neuro_bank.module.user.entity.User;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class JwtTokenProvider {
  private final RSAKey rsaKey;
  @Value("${app.jwt.issuer}")
  private String issuer;
  @Value("${app.jwt.access-token-expiry}")
  private long accessTokenExpiry;
  @Value("${app.jwt.refresh-token-expiry}")
  private long refreshTokenExpiry;

  public JwtTokenProvider(
      @Value("${app.jwt.private-key-location}") RSAPrivateKey privateKey,
      @Value("${spring.security.oauth2.resourceserver.jwt.public-key-location}") RSAPublicKey publicKey
  ) {
    this.rsaKey = new RSAKey.Builder(publicKey)
        .privateKey(privateKey)
        .keyID(UUID.randomUUID().toString())
        .build();
  }

  public String generateAccessToken(User user, String deviceFingerprint, String sessionId) {
    return buildToken(user, accessTokenExpiry, "ACCESS", deviceFingerprint, sessionId);
  }

  public String generateRefreshToken(User user, String deviceFingerprint, String sessionId) {
    return buildToken(user, refreshTokenExpiry, "REFRESH", deviceFingerprint, sessionId);
  }

  private String buildToken(User user, long expirySeconds, String tokenType,
                            String deviceFingerprint, String sessionId) {
    try {
      Instant now = Instant.now();
      JWTClaimsSet claims = new JWTClaimsSet.Builder()
          .subject(user.getId().toString())
          .issuer(issuer)
          .issueTime(Date.from(now))
          .expirationTime(Date.from(now.plusSeconds(expirySeconds)))
          .jwtID(UUID.randomUUID().toString())
          .claim("email", user.getEmail())
          .claim("status", user.getStatus().name())
          .claim("tokenType", tokenType)
          .claim("sessionId", sessionId)
          .claim("deviceFingerprint", deviceFingerprint)
          .build();

      SignedJWT signedJWT = new SignedJWT(
          new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
          claims);
      signedJWT.sign(new RSASSASigner(rsaKey));
      return signedJWT.serialize();
    } catch (Exception e) {
      throw new RuntimeException("Cannot generate JWT", e);
    }
  }

  public JWTClaimsSet parseToken(String token) {
    try {
      SignedJWT jwt = SignedJWT.parse(token);
      if (!jwt.verify(new RSASSAVerifier(rsaKey.toRSAPublicKey())))
        throw BusinessException.unauthorized("Invalid token signature");

      JWTClaimsSet claims = jwt.getJWTClaimsSet();
      if (claims.getExpirationTime().before(new java.util.Date()))
        throw BusinessException.unauthorized("Token expired");

      return claims;
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw BusinessException.unauthorized("Invalid token");
    }
  }

  public String extractJti(String token) {
    try {
      return SignedJWT.parse(token).getJWTClaimsSet().getJWTID();
    } catch (Exception e) {
      return null;
    }
  }

  public Duration getRemainingTtl(String token) {
    try {
      Date expiry = (Date) SignedJWT.parse(token).getJWTClaimsSet().getExpirationTime();
      long seconds = (expiry.getTime() - System.currentTimeMillis()) / 1000;
      return Duration.ofSeconds(Math.max(seconds, 0));
    } catch (Exception e) {
      return Duration.ZERO;
    }
  }
}
