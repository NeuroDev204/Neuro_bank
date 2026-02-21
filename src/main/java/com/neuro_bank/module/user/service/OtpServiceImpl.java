package com.neuro_bank.module.user.service;

import com.neuro_bank.module.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {
  @Override
  public void sendOtp(User user, String type) {

  }

  @Override
  public void verifyOtp(User user, String type, String code) {

  }

  @Override
  public void invalidateOtp(User user, String type) {

  }
}
