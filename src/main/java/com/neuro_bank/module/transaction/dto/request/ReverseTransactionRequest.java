package com.neuro_bank.module.transaction.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReverseTransactionRequest {
  @NotBlank(message = "reason is required")
  @Size(max = 255, message = "reason max length is 255")
  private String reason;
}
