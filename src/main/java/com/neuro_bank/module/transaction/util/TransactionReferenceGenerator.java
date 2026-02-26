package com.neuro_bank.module.transaction.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

@Component
public class TransactionReferenceGenerator {
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
  private final SecureRandom secureRandom = new SecureRandom();

  public String nextReference() {
    String timestamp = LocalDateTime.now(ZoneOffset.UTC).format(FORMATTER);
    int randomNumber = secureRandom.nextInt(1_000_000);
    return "TXN" + timestamp + String.format("%06d", randomNumber);
  }
}
