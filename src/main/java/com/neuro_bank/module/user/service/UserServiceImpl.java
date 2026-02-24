package com.neuro_bank.module.user.service;

import com.neuro_bank.common.enums.KycStatus;
import com.neuro_bank.common.enums.UserStatus;
import com.neuro_bank.common.exception.BusinessException;
import com.neuro_bank.module.notification.service.AuditLogService;
import com.neuro_bank.module.user.dto.request.RegisterRequest;
import com.neuro_bank.module.user.dto.request.UpdateProfileRequest;
import com.neuro_bank.module.user.dto.request.VerifyOtpRequest;
import com.neuro_bank.module.user.dto.response.UserResponse;
import com.neuro_bank.module.user.entity.User;
import com.neuro_bank.module.user.entity.UserCredential;
import com.neuro_bank.module.user.repository.UserCredentialRepository;
import com.neuro_bank.module.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final UserCredentialRepository userCredentialRepository;
  private final OtpService otpService;
  private final AuditLogService auditLogService;
  private final PasswordEncoder passwordEncoder;
  private final PasswordEncoder pinEncoder;

  public UserServiceImpl(UserRepository userRepository,
      UserCredentialRepository userCredentialRepository,
      OtpService otpService,
      AuditLogService auditLogService,
      PasswordEncoder passwordEncoder,
      @Qualifier("pinEncoder") PasswordEncoder pinEncoder) {
    this.userRepository = userRepository;
    this.userCredentialRepository = userCredentialRepository;
    this.otpService = otpService;
    this.auditLogService = auditLogService;
    this.passwordEncoder = passwordEncoder;
    this.pinEncoder = pinEncoder;
  }

  @Override
  public UserResponse register(RegisterRequest request) {
    // validate info
    if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
      throw BusinessException.conflict("Email already registered");
    }
    if (userRepository.existsByPhoneAndDeletedFalse(request.getPhone())) {
      throw BusinessException.conflict("Phone already registered");
    }
    if (userRepository.existsByNationalIdAndDeletedFalse(request.getNationalId())) {
      throw BusinessException.conflict("National ID already registered");
    }
    if (Period.between(request.getDateOfBirth(), LocalDate.now()).getYears() < 18) {
      throw BusinessException.badRequest("Must be at least 18 years old");
    }

    // create user
    User user = new User();
    user.setFullName(request.getFullName());
    user.setEmail(request.getEmail());
    user.setPhone(request.getPhone());
    user.setNationalId(request.getNationalId());
    user.setDateOfBirth(request.getDateOfBirth());
    user.setAddress(request.getAddress());
    user.setStatus(UserStatus.PENDING_VERIFICATION);
    user.setKycStatus(KycStatus.PENDING);
    userRepository.save(user);

    // create credential
    UserCredential credential = new UserCredential();
    credential.setUser(user);
    credential.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    credential.setPinHash(pinEncoder.encode(request.getPin()));
    credential.setFailedLoginAttempts(0);
    credential.setFailedPinAttempts(0);
    credential.setTwoFactorEnabled(false);
    userCredentialRepository.save(credential);

    // send otp
    otpService.sendOtp(user, "EMAIL_VERIFICATION");

    // ghi log
    runAfterCommit(() -> auditLogService.longAsync(user, "REGISTER", "USER", user.getId(), true, null, null));
    return UserResponse.from(user);
  }

  @Override
  public void verifyEmail(VerifyOtpRequest request) {
    User user = userRepository.findByEmailWithCredential(request.getEmail())
        .orElseThrow(() -> BusinessException.notFound("User"));
    if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
      throw BusinessException.badRequest("Account already verified");
    }
    otpService.verifyOtp(user, "EMAIL_VERIFICATION", request.getCode());
    user.setStatus(UserStatus.ACTIVE);
    userRepository.save(user);
    auditLogService.longAsync(user, "EMAIL_VERIFIED", "USER", user.getId(), true, null, null);
  }

  @Override
  public void resendOtp(String email) {
    User user = userRepository.findByEmailWithCredential(email)
        .orElseThrow(() -> BusinessException.notFound("User"));
    if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
      throw BusinessException.badRequest("Account already verified");
    }
    otpService.sendOtp(user, "EMAIL_VERIFICATION");
  }

  @Override
  @Transactional
  public UserResponse getById(UUID id) {
    return userRepository.findByIdWithCredential(id)
        .map(UserResponse::from)
        .orElseThrow(() -> BusinessException.notFound("User"));
  }

  @Override
  public UserResponse updateProfile(UUID id, UpdateProfileRequest request) {
    User user = userRepository.findByIdWithCredential(id)
        .orElseThrow(() -> BusinessException.notFound("User"));
    if (request.getFullName() != null)
      user.setFullName(request.getFullName());
    if (request.getAddress() != null)
      user.setAddress(request.getAddress());
    if (request.getAvatarUrl() != null)
      user.setAvatarUrl(request.getAvatarUrl());
    userRepository.save(user);
    auditLogService.longAsync(user, "UPDATE_PROFILE", "USER", user.getId(), true, null, null);
    return UserResponse.from(user);
  }

  private void runAfterCommit(Runnable action) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          action.run();
        }
      });
      return;
    }
    action.run();
  }
}


