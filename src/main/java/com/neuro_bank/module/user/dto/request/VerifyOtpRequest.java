package com.neuro_bank.module.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder

public class VerifyOtpRequest {
  @NotBlank
  @Email
  private String email;

  @NotBlank
  @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
  private String code;
}
