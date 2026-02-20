package com.neuro_bank.security;

import com.neuro_bank.module.user.entity.User;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.Date;
import java.time.Instant;
import java.util.UUID;

@Component
public class JwtTokenProvider {
  @Value("${app.jwt.issuer}")
  private String issuer;
  @Value("${app.jwt.access-token-expiry}")
  private long accessTokenExpiry;
  @Value("${app.jwt.refresh-token-expiry}")
  private long refreshTokenExpiry;
  private final RSAKey rsaKey;

  public JwtTokenProvider(
      @Value("${app.jwt.private-key-location}") RSAPrivateKey privateKey,
      @Value("${spring.security.oauth2.resourceserver.jwt.public-key-location}") RSAPublicKey publicKey
  ) {
    this.rsaKey = new RSAKey.Builder(publicKey)
        .privateKey(privateKey)
        .keyID(UUID.randomUUID().toString())
        .build();
  }

  public String generateAccessToken(User user){
    return buildToken(user, accessTokenExpiry, "ACCESS");
  }
  public String generateRefreshToken(User user){
    return buildToken(user,refreshTokenExpiry,"REFRESH");
  }
  public String buildToken(User user,long expirySeconds, String tokenType){
    try{
      JWSSigner signer = new RSASSASigner(rsaKey);
      Instant now = Instant.now();

      JWTClaimsSet claims = new JWTClaimsSet.Builder()
          .subject(user.getId().toString())
          .issuer(issuer)
          .issueTime(Date.from(now))
          .expirationTime(java.util.Date.from(now.plusSeconds(expirySeconds)))
          .jwtID(UUID.randomUUID().toString())
          .claim("email", user.getEmail())
          .claim("phone", user.getPhone())
          .claim("status", user.getStatus().name())
          .claim("tokenType", tokenType)
          .build();
      SignedJWT signedJWT = new SignedJWT(
          new JWSHeader.Builder(JWSAlgorithm.RS256)
              .keyID(rsaKey.getKeyID())
              .build(),
          claims
      );
      signedJWT.sign(signer);
      return signedJWT.serialize();
    }catch (Exception e){
      throw new RuntimeException("Cannot generate JWT token");
    }
  }
  public JWTClaimsSet parseToken(String token){
    try{
      SignedJWT jwt = SignedJWT.parse(token);
      JWSVerifier verifier = new RSASSAVerifier(rsaKey.toRSAPublicKey());
      if(!jwt.verify(verifier)){
        throw new RuntimeException("Invalid token signature");
      }
      JWTClaimsSet claims = jwt.getJWTClaimsSet();
      if(claims.getExpirationTime().before(new java.util.Date())){
        throw new RuntimeException("Token expired");
      }
      return claims;
    }catch (Exception e){
      throw new RuntimeException("Invalid token");
    }
  }
}
