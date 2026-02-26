package com.neuro_bank.module.transaction.util;

import java.math.BigDecimal;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.neuro_bank.module.transaction.dto.request.CreateTransferRequest;

@Component
public class TransactionHashService {
  @Value("${app.transaction.hash.secret}")
  private String hashSecret;

  public String hashIdempotencyKey(UUID userId, String endpoint, String rawIdempotencyKey) {
    String raw = userId + ":" + endpoint + ":" + rawIdempotencyKey + ":" + hashSecret;
    return DigestUtils.sha256Hex(raw);
  }

  public String hashTransferRequest(CreateTransferRequest request) {
    String description = request.getDescription() == null ? "" : request.getDescription().trim().toLowerCase();
    String amount = normalizeAmount(request.getAmount());
    String raw = request.getFromAccountId() + ":" + request.getToAccountId() + ":" + amount + ":"
        + request.getCurrency() + ":" + description;
    return DigestUtils.sha256Hex(raw);
  }

  public String maskAccountNumber(String accountNumber) {
    if (accountNumber == null || accountNumber.length() < 8) {
      return "****";
    }
    return accountNumber.substring(0, 4) + "******" + accountNumber.substring(accountNumber.length() - 4);
  }

  private String normalizeAmount(BigDecimal amount) {
    return amount == null ? "0" : amount.stripTrailingZeros().toPlainString();
  }
}
