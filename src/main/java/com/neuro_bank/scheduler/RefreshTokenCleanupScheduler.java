package com.neuro_bank.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.neuro_bank.module.user.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCleanupScheduler {
  private final RefreshTokenRepository refreshTokenRepository;

  @Scheduled(cron = "0 0 3 * * *") // 3am
  @Transactional
  public void cleanup() {
    refreshTokenRepository.deleteExpiredAndRevoked(LocalDateTime.now());
  }
}
