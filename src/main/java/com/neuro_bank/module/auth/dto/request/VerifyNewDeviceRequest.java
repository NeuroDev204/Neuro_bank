package com.neuro_bank.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class VerifyNewDeviceRequest {
  @NonNull
  private UUID userId;
  @NotBlank
  @Pattern(regexp = "^\\d{6}$",message = "OTP must be 6 digits")
  private String otpCode;

}
