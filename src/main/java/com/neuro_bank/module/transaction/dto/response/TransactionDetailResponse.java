package com.neuro_bank.module.transaction.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.neuro_bank.module.transaction.entity.Transaction;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TransactionDetailResponse {
  private TransactionResponse sumary;
  private String parentReferenceNo;
  private List<TransactionEntryResponse> entries;
  private Map<String, Object> metadata;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static TransactionDetailResponse from(Transaction transaction) {
    return TransactionDetailResponse.builder()
        .sumary(TransactionResponse.from(transaction))
        .parentReferenceNo(
            transaction.getParentTransaction() != null ? transaction.getParentTransaction().getReferenceNo() : null)
        .entries(transaction.getEntries().stream().map(TransactionEntryResponse::from).toList())
        .metadata(transaction.getMetadata())
        .createdAt(transaction.getCreatedAt())
        .updatedAt(transaction.getUpdatedAt())
        .build();
  }
}
