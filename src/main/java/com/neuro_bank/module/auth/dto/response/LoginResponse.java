package com.neuro_bank.module.auth.dto.response;

import com.neuro_bank.common.enums.KycStatus;
import com.neuro_bank.common.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LoginResponse {
  private UUID id;
  private String fullName;
  private String email;
  private String phone;
  private UserStatus status;
  private KycStatus kycStatus;
}
