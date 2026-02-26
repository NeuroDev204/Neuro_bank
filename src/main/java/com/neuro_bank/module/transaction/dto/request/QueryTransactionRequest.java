package com.neuro_bank.module.transaction.dto.request;

import com.neuro_bank.common.enums.TransactionStatus;
import com.neuro_bank.common.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class QueryTransactionRequest {
  private TransactionStatus status;
  private TransactionType type;
  private LocalDateTime fromDate;
  private LocalDateTime toDate;

  @Builder.Default
  private int page = 0;
  @Builder.Default
  private int size = 20;
}
