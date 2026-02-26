package com.neuro_bank.module.transaction.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PagedTransactionResponse {
  private List<TransactionResponse> items;
  private long totalElements;
  private int totalPages;
  private int page;
  private int size;
  private boolean hasNext;
}
