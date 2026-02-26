package com.neuro_bank.module.transaction.dto.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CreateTransferRequest {
  @NotNull(message = "fromAccountId is required")
  private UUID fromAccountId;
  @NotNull(message = "toAccountId is required")
  private UUID toAccountId;
  @NotNull(message = "amount is required")
  @DecimalMin(value = "0.01", message = "amount must be greater than 0")
  private BigDecimal amount;
  @NotNull(message = "currency is required")
  private Currency currency;
  @Size(max = 255, message = "description max length is 255")
  private String description;
}
