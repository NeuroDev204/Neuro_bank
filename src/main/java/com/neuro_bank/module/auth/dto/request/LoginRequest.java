package com.neuro_bank.module.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
  @NotBlank
  @Email
  private String email;
  @NotBlank
  @Size(min = 8,max = 100)
  private String password;

  // SHA-256(userAgent + screenResolution + timezone + platform) — tính từ client
  @Size(max = 64)
  private String deviceFingerprint;
}
