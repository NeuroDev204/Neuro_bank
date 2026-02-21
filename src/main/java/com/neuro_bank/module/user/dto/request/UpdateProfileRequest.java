package com.neuro_bank.module.user.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder

public class UpdateProfileRequest {
  @Size(min = 2,max = 100)
  private String fullName;
  @Size(max = 255)
  private String address;
  @Size(max = 255)
  private String avatarUrl;
}
