package com.neuro_bank.module.auth.service;

import com.neuro_bank.common.enums.UserStatus;
import com.neuro_bank.module.auth.dto.request.LoginRequest;
import com.neuro_bank.module.auth.dto.response.LoginResponse;
import com.neuro_bank.module.user.entity.RefreshToken;
import com.neuro_bank.module.user.entity.User;
import com.neuro_bank.module.user.entity.UserCredential;
import com.neuro_bank.module.user.repository.RefreshTokenRepository;
import com.neuro_bank.module.user.repository.UserCredentialRepository;
import com.neuro_bank.module.user.repository.UserRepository;
import com.neuro_bank.security.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

  private static final int MAX_FAILED_ATTEMPTS = 5;
  private static final int LOCK_DURATION_MINUTES = 30;
  UserRepository userRepository;
  UserCredentialRepository userCredentialRepository;
  RefreshTokenRepository refreshTokenRepository;
  JwtTokenProvider jwtTokenProvider;
  PasswordEncoder passwordEncoder;

  @Override
  public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
    User user = userRepository.findByEmailWithCredential(request.getEmail())
        .orElseThrow(() -> new RuntimeException("Invalid credentials"));
    UserCredential credential = user.getCredential();
    // check account bi lock chua
    if (credential.getLockedUntil() != null &&
        credential.getLockedUntil().isAfter(LocalDateTime.now())) {
      long minutesLeft = ChronoUnit.MINUTES.between(
          LocalDateTime.now(), credential.getLockedUntil());
      throw new RuntimeException("Account locked. Try again in " + minutesLeft);
    }
    // verify password
    if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
      handleFailedLogin(credential, user, ipAddress);
      throw new RuntimeException("Invalid credentials");
    }
    //check user status
    if (user.getStatus() == UserStatus.SUSPENDED) {
      throw new RuntimeException("Account suspended");
    }
    if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
      throw new RuntimeException("Please verify your account");
    }
    //reset failed attempts
    credential.setFailedPinAttempts(0);
    credential.setLockedUntil(null);
    credential.setLastLoginAt(LocalDateTime.now());
    credential.setLastLoginIp(ipAddress);
    userCredentialRepository.save(credential);

//    String accessToken = jwtTokenProvider.generateAccessToken(user);
//    String refreshToken = jwtTokenProvider.generateRefreshToken(user);
//    saveRefreshToken(user,refreshToken,ipAddress,userAgent);

    return null;
    //return new LoginResult(accessToken,refreshToken,)
  }

  @Override
  public LoginResponse refreshToken(String refreshToken, String ipAddress) {
    return null;
  }

  @Override
  public void logout(String refreshToken, UUID userId) {

  }

  private void handleFailedLogin(UserCredential credential, User user, String ip) {
    int attempts = credential.getFailedPinAttempts() + 1;
    credential.setFailedPinAttempts(attempts);
    if (attempts >= MAX_FAILED_ATTEMPTS) {
      credential.setLockedUntil(
          LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES)
      );
    }
    userCredentialRepository.save(credential);
  }

  private void saveRefreshToken(User user, String token, String ip, String userAgent) {
    RefreshToken entity = new RefreshToken();
    entity.setUser(user);
    entity.setTokenHash(hashToken(token));
    entity.setExpiresAt(LocalDateTime.now().plusSeconds(604800));
    entity.setIpAddress(ip);
    entity.setUserAgent(userAgent);
    refreshTokenRepository.save(entity);
  }

  private String hashToken(String token) {
    // sha-256 hash
    return DigestUtils.sha256Hex(token);
  }
}
