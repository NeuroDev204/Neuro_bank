package com.neuro_bank.module.user.service;

import com.neuro_bank.common.exception.BusinessException;
import com.neuro_bank.infrastructure.EmailService;
import com.neuro_bank.infrastructure.RateLimiterService;
import com.neuro_bank.infrastructure.RedisService;
import com.neuro_bank.module.user.entity.Otp;
import com.neuro_bank.module.user.entity.User;
import com.neuro_bank.module.user.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

  private static final int OTP_EXPIRY_MINUTES = 5;
  private static final int MAX_VERIFY_ATTEMPTS = 5;
  private final OtpRepository otpRepository;
  private final RedisService redisService;
  private final RateLimiterService rateLimiterService;
  private final EmailService emailService;

  @Override
  @Transactional
  public void sendOtp(User user, String type) {
    rateLimiterService.checkOtpSendRateLimit(user.getId(), type);
    otpRepository.invalidateExisting(user, type, LocalDateTime.now());

    String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));
    String codeHash = DigestUtils.sha256Hex(code);

    Otp otp = new Otp();
    otp.setUser(user);
    otp.setCode(codeHash);
    otp.setType(type);
    otp.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
    otpRepository.save(otp);
    emailService.sendOtp(user.getEmail(), user.getFullName(), code, type);
  }

  @Override
  @Transactional
  public void verifyOtp(User user, String type, String code) {
    String attemptKey = "otp:attempt:" + user.getId() + ":" + type;
    long attempts = redisService.increment(attemptKey, Duration.ofMinutes(OTP_EXPIRY_MINUTES));
    if (attempts > MAX_VERIFY_ATTEMPTS) {
      throw BusinessException.toManyRequests("Too many failed attempts. Please request a new OTP");
    }
    String codeHash = DigestUtils.sha256Hex(code);
    Otp otp = otpRepository.findValidOtp(user, type, codeHash, LocalDateTime.now());
    if (otp == null) {
      throw BusinessException.badRequest("Invalid or expired");
    }
    otp.setUsed(true);
    otp.setUsedAt(LocalDateTime.now());
    otpRepository.save(otp);
    redisService.delete(attemptKey);
  }

  @Override
  @Transactional
  public void invalidateOtp(User user, String type) {
    otpRepository.invalidateExisting(user, type, LocalDateTime.now());
  }
}
