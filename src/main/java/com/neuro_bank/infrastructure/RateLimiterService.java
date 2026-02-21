package com.neuro_bank.infrastructure;

import com.neuro_bank.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RateLimiterService {
  private final RedisService redisService;

  public void checkRateLimit(String key, int maxRequests, Duration window) {
    long count = redisService.increment(key, window);
    if (count > maxRequests) {
      throw BusinessException.toManyRequests("To many requests. Please try again later");
    }
  }

  public void checkLoginRateLimitByIp(String ip) {
    checkRateLimit("rate:login:ip:" + ip, 10, Duration.ofMinutes(15));
  }

  public void checkLoginRateLimitByEmail(String email) {
    //hash email de khong luu plaintext trong redis key
    checkRateLimit("rate:login:email:" + DigestUtils.sha256Hex(email), 5, Duration.ofMinutes(15));
  }

  public void checkOtpSendRateLimit(UUID userId, String type) {
    checkRateLimit("rate:otp:send:" + userId + ":" + type, 3, Duration.ofMinutes(15));
  }
}
