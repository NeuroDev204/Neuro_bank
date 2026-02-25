package com.neuro_bank.module.auth.service;

import com.neuro_bank.common.enums.UserStatus;
import com.neuro_bank.common.exception.BusinessException;
import com.neuro_bank.common.exception.NewDeviceException;
import com.neuro_bank.infrastructure.RateLimiterService;
import com.neuro_bank.infrastructure.RedisService;
import com.neuro_bank.module.auth.dto.request.LoginRequest;
import com.neuro_bank.module.auth.dto.request.VerifyNewDeviceRequest;
import com.neuro_bank.module.notification.service.AuditLogService;
import com.neuro_bank.module.user.dto.response.UserResponse;
import com.neuro_bank.module.user.entity.RefreshToken;
import com.neuro_bank.module.user.entity.User;
import com.neuro_bank.module.user.entity.UserCredential;
import com.neuro_bank.module.user.entity.UserDevice;
import com.neuro_bank.module.user.repository.RefreshTokenRepository;
import com.neuro_bank.module.user.repository.UserCredentialRepository;
import com.neuro_bank.module.user.repository.UserDeviceRepository;
import com.neuro_bank.module.user.repository.UserRepository;
import com.neuro_bank.module.user.service.OtpService;
import com.neuro_bank.security.JwtTokenProvider;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

  private static final int MAX_FAILED_ATTEMPTS = 5;
  private static final int LOCK_MINUTES = 30;
  private final UserRepository userRepository;
  private final UserCredentialRepository userCredentialRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserDeviceRepository userDeviceRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final AuditLogService auditLogService;
  private final OtpService otpService;
  private final RedisService redisService;
  private final RateLimiterService rateLimiterService;
  private final PasswordEncoder passwordEncoder;

  @Override
  public LoginResult login(LoginRequest request, String ipAddress, String userAgent, String deviceFingerprint) {

    // check limit
    rateLimiterService.checkLoginRateLimitByIp(ipAddress);
    rateLimiterService.checkLoginRateLimitByEmail(request.getEmail());

    User user = userRepository.findByEmailWithCredential(request.getEmail())
        .orElseThrow(() -> BusinessException.unauthorized("Invalid credential"));
    UserCredential userCredential = user.getCredential();

    // check lock
    if (userCredential.getLockedUntil() != null && userCredential.getLockedUntil().isAfter(LocalDateTime.now())) {
      long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), userCredential.getLockedUntil());
      throw BusinessException.unauthorized("Account locked. Try again in " + minutes + " minutes");
    }
    // check pass
    if (!passwordEncoder.matches(request.getPassword(), userCredential.getPasswordHash())) {
      handleFailedLogin(userCredential, user, ipAddress, userAgent);
      throw BusinessException.unauthorized("Invalid credentials");
    }
    if (user.getStatus() == UserStatus.SUSPENDED) {
      throw BusinessException.forbidden("Account suspended, Please contact support");
    }
    if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
      throw BusinessException.forbidden("Please verify your email first");
    }

    // check new device
    boolean isNewDevice = handleDevice(user, deviceFingerprint, userAgent, ipAddress);
    if (isNewDevice) {
      otpService.sendOtp(user, "NEW_DEVICE_LOGIN");
      redisService.set("pending:login:" + user.getId(), deviceFingerprint + "|" + ipAddress, Duration.ofMinutes(15));
      throw new NewDeviceException(user.getId(), "New device detected. OTP sent to your email");
    }
    userCredential.setFailedLoginAttempts(0);
    userCredential.setLockedUntil(null);
    userCredential.setLastLoginAt(LocalDateTime.now());
    userCredential.setLastLoginIp(ipAddress);
    userCredentialRepository.save(userCredential);
    return issueTokens(user, deviceFingerprint, ipAddress, userAgent);
  }

  @Override
  public LoginResult verifyNewDevice(VerifyNewDeviceRequest request, String ipAddress, String userAgent) {
    User user = userRepository.findByIdWithCredential(request.getUserId())
        .orElseThrow(() -> BusinessException.notFound("User"));
    otpService.verifyOtp(user, "NEW_DEVICE_LOGIN", request.getOtpCode());

    String pendingKey = "pending:login:" + user.getId();
    String pendingValue = redisService.get(pendingKey)
        .orElseThrow(() -> BusinessException.unauthorized("Session expired. please login again"));
    String deviceFingerprint = pendingValue.split("\\|")[0];
    redisService.delete(pendingKey);
    userDeviceRepository.findByUserAndDeviceFingerprint(user, deviceFingerprint)
        .ifPresent(d -> {
          d.setTrusted(true);
          d.setTrustedAt(LocalDateTime.now());
          userDeviceRepository.save(d);
        });

    return issueTokens(user, deviceFingerprint, ipAddress, userAgent);
  }

  @Override
  public RefreshResult refreshAccessToken(String refreshToken, String ipAddress, String deviceFingerprint) {

    JWTClaimsSet claimsSet = jwtTokenProvider.parseToken(refreshToken);

    if (!"REFRESH".equals(claimsSet.getClaim("tokenType"))) {
      throw BusinessException.unauthorized("Invalid token type");
    }

    String tokenDevice = (String) claimsSet.getClaim("deviceFingerprint");

    if (deviceFingerprint != null && !deviceFingerprint.equals(tokenDevice)) {
      throw BusinessException.unauthorized("Device mismatch detected");
    }

    RefreshToken stored = refreshTokenRepository
        .findByTokenHash(DigestUtils.sha256Hex(refreshToken))
        .orElseThrow(() -> BusinessException.unauthorized("Refresh token not found"));

    if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw BusinessException.unauthorized("Refresh token expired");
    }

    if (stored.isRevoked()) {

      refreshTokenRepository.revokeByFamilyId(stored.getFamilyId(), LocalDateTime.now());

      auditLogService.longAsync(
          stored.getUser(),
          "REFRESH_TOKEN_REUSE",
          "USER",
          stored.getUser().getId(),
          false,
          ipAddress,
          null
      );

      throw BusinessException.unauthorized("Security violation. Please login again");
    }

    stored.setRevoked(true);
    stored.setRevokedAt(LocalDateTime.now());
    refreshTokenRepository.save(stored);

    String newAccessToken = jwtTokenProvider.generateAccessToken(stored.getUser(), deviceFingerprint, stored.getSessionId());

    String newRefreshToken = jwtTokenProvider.generateRefreshToken(
        stored.getUser(),
        deviceFingerprint,
        stored.getSessionId()
    );

    saveRefreshToken(
        stored.getUser(),
        newRefreshToken,
        ipAddress,
        stored.getUserAgent(),
        stored.getFamilyId(),
        stored.getGeneration() + 1,
        stored.getSessionId()
    );

    return new RefreshResult(newAccessToken, newRefreshToken);
  }

  @Override
  public void logout(String refreshToken, String accessToken, UUID userId) {
    if (refreshToken != null) {
      refreshTokenRepository.findByTokenHash(DigestUtils.sha256Hex(refreshToken))
          .ifPresent(t -> refreshTokenRepository.revokeByFamilyId(t.getFamilyId(), LocalDateTime.now()));
    }
    if (accessToken != null) {
      String jti = jwtTokenProvider.extractJti(accessToken);
      Duration ttl = jwtTokenProvider.getRemainingTtl(accessToken);
      if (jti != null && !ttl.isZero()) {
        redisService.blacklistToken(jti, ttl);
      }
    }
  }

  private LoginResult issueTokens(User user, String deviceFingerprint, String ipAddress, String userAgent) {
    String sessionId = UUID.randomUUID().toString();
    String familyId = UUID.randomUUID().toString();
    String accessToken = jwtTokenProvider.generateAccessToken(user, deviceFingerprint, sessionId);
    String refreshToken = jwtTokenProvider.generateRefreshToken(user, deviceFingerprint, sessionId);
    saveRefreshToken(user, refreshToken, ipAddress, userAgent, familyId, 1, sessionId);
    if (deviceFingerprint != null) {
      userDeviceRepository.findByUserAndDeviceFingerprint(user, deviceFingerprint)
          .ifPresent(d -> {
            d.setLastSeenAt(LocalDateTime.now());
            d.setLastIpAddress(ipAddress);
            d.setLoginCount(d.getLoginCount() + 1);
            userDeviceRepository.save(d);
          });
    }
    auditLogService.longAsync(user, "LOGIN", "USER", user.getId(), true, ipAddress, userAgent);
    return new LoginResult(accessToken, refreshToken, UserResponse.from(user));
  }

  private boolean handleDevice(User user, String fingerprint, String userAgent, String ip) {
    if (fingerprint == null)
      return false;
    return userDeviceRepository.findByUserAndDeviceFingerprint(user, fingerprint)
        .map(d -> false)
        .orElseGet(() -> {
          UserDevice device = new UserDevice();
          device.setUser(user);
          device.setDeviceFingerprint(fingerprint);
          device.setDeviceName(parseDeviceName(userAgent));
          device.setDeviceType(parseDeviceType(userAgent));
          device.setTrusted(false);
          device.setLastIpAddress(ip);
          device.setLastSeenAt(LocalDateTime.now());
          userDeviceRepository.save(device);
          return true;
        });
  }

  private void saveRefreshToken(User user, String token, String ip, String userAgent, String familyId, int generation,
                                String sessionId) {
    RefreshToken entity = new RefreshToken();
    entity.setUser(user);
    entity.setTokenHash(DigestUtils.sha256Hex(token));
    entity.setExpiresAt(LocalDateTime.now().plusDays(7));
    entity.setIpAddress(ip);
    entity.setUserAgent(userAgent);
    entity.setFamilyId(familyId);
    entity.setGeneration(generation);
    entity.setSessionId(sessionId);
    refreshTokenRepository.save(entity);
  }

  private void handleFailedLogin(UserCredential credential, User user, String ip, String userAgent) {
    int attempts = credential.getFailedLoginAttempts() + 1;
    credential.setFailedLoginAttempts(attempts);
    if (attempts >= MAX_FAILED_ATTEMPTS) {
      credential.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
      auditLogService.longAsync(user, "ACCOUNT_LOCKED", "USER", user.getId(), false, ip, userAgent);
    }
    userCredentialRepository.save(credential);
  }

  private String parseDeviceName(String ua) {
    if (ua == null)
      return "Unknown";
    if (ua.contains("iPhone"))
      return "Safari on iPhone";
    if (ua.contains("Android"))
      return "Chrome on Android";
    if (ua.contains("Windows"))
      return "Chrome on Windows";
    if (ua.contains("Macintosh"))
      return "Safari on Mac";
    return "Unknown Device";
  }

  private String parseDeviceType(String ua) {
    if (ua == null)
      return "UNKNOWN";
    if (ua.contains("Mobile") || ua.contains("iPhone") || ua.contains("Android"))
      return "MOBILE";
    if (ua.contains("iPad") || ua.contains("Tablet"))
      return "TABLET";
    return "DESKTOP";
  }
}
