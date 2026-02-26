package com.neuro_bank.module.transaction.dto.response;

import com.neuro_bank.common.enums.EntryType;
import com.neuro_bank.module.transaction.entity.TransactionEntry;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TransactionEntryResponse {
  private UUID accountId;
  private String accountNumber;
  private EntryType entryType;
  private BigDecimal amount;
  private BigDecimal balanceBefore;
  private BigDecimal balanceAfter;

  public static TransactionEntryResponse from(TransactionEntry entry) {
    return TransactionEntryResponse.builder()
        .accountId(entry.getAccount().getId())
        .accountNumber(entry.getAccount().getAccountNumber())
        .entryType(entry.getEntryType())
        .amount(entry.getAmount())
        .balanceBefore(entry.getBalanceBefore())
        .balanceAfter(entry.getBalanceAfter())
        .build();
  }
}
