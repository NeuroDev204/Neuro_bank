package com.neuro_bank.module.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.neuro_bank.common.enums.KycStatus;
import com.neuro_bank.common.enums.UserStatus;
import com.neuro_bank.module.user.entity.User;
import com.neuro_bank.security.UserPrincipal;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

  private UUID id;
  private String fullName;
  private String email;
  private String phone;
  private String nationalId;
  private LocalDate dateOfBirth;
  private String address;
  private String avatarUrl;
  private UserStatus status;
  private KycStatus kycStatus;
  private LocalDateTime kycVerifiedAt;
  private LocalDateTime createdAt;

  public static UserResponse from(User user) {
    return UserResponse.builder()
        .id(user.getId()).fullName(user.getFullName())
        .email(user.getEmail()).phone(user.getPhone())
        .nationalId(user.getNationalId()).dateOfBirth(user.getDateOfBirth())
        .address(user.getAddress()).avatarUrl(user.getAvatarUrl())
        .status(user.getStatus()).kycStatus(user.getKycStatus())
        .kycVerifiedAt(user.getKycVerifiedAt()).createdAt(user.getCreatedAt())
        .build();
  }

  public static UserResponse from(UserPrincipal principal) {
    return UserResponse.builder()
        .id(principal.getId()).email(principal.getUsername())
        .phone(principal.getPhone()).status(principal.getStatus())
        .build();
  }
}