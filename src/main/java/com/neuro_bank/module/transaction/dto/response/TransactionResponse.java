package com.neuro_bank.module.transaction.dto.response;

import com.neuro_bank.common.enums.Currency;
import com.neuro_bank.common.enums.EntryType;
import com.neuro_bank.common.enums.TransactionStatus;
import com.neuro_bank.common.enums.TransactionType;
import com.neuro_bank.module.transaction.entity.Transaction;
import com.neuro_bank.module.transaction.entity.TransactionEntry;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TransactionResponse {
  private UUID id;
  private String referenceNo;
  private TransactionType type;
  private TransactionStatus status;
  private BigDecimal amount;
  private BigDecimal fee;
  private BigDecimal totalAmount;
  private Currency currency;
  private String description;
  private UUID fromAccountId;
  private String fromAccountNumber;
  private UUID toAccountId;
  private String toAccountNumber;
  private LocalDateTime completedAt;
  private LocalDateTime createdAt;

  public static TransactionResponse from(Transaction transaction) {
    TransactionEntry debit = findFirstEntry(transaction, EntryType.DEBIT);
    TransactionEntry credit = findFirstEntry(transaction, EntryType.CREDIT);

    return TransactionResponse.builder()
        .id(transaction.getId())
        .referenceNo(transaction.getReferenceNo())
        .type(transaction.getType())
        .status(transaction.getStatus())
        .amount(transaction.getAmount())
        .fee(transaction.getFee())
        .totalAmount(transaction.getTotalAmount())
        .currency(transaction.getCurrency())
        .description(transaction.getDescription())
        .fromAccountId(debit != null ? debit.getAccount().getId() : null)
        .fromAccountNumber(debit != null ? debit.getAccount().getAccountNumber() : null)
        .toAccountId(credit != null ? credit.getAccount().getId() : null)
        .toAccountNumber(credit != null ? credit.getAccount().getAccountNumber() : null)
        .completedAt(transaction.getCompletedAt())
        .createdAt(transaction.getCreatedAt())
        .build();
  }

  private static TransactionEntry findFirstEntry(Transaction transaction, EntryType entryType) {
    if (transaction.getEntries() == null || transaction.getEntries().isEmpty()) {
      return null;
    }
    return transaction.getEntries().stream()
        .filter(entry -> entry.getEntryType() == entryType)
        .findFirst()
        .orElse(null);
  }
}
