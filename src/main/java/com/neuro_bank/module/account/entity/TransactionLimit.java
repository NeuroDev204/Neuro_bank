package com.neuro_bank.module.account.entity;

import com.neuro_bank.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_limits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TransactionLimit extends BaseEntity {
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false,unique = true)
  Account account;
  @Column(nullable = false, precision = 19,scale = 4)
  @Builder.Default
  BigDecimal dailyTransferLimit = new BigDecimal("100000000"); // 100m vnd
  @Column(nullable = false,precision = 19, scale = 4)
  @Builder.Default
  BigDecimal perTransactionLimit = new BigDecimal("50000000"); //50m  vnd
  @Column(nullable = false, precision = 19, scale = 4)
  BigDecimal monthlyTransferLimit = new BigDecimal("1000000000"); //1b vnd


  // track usage - reset daily/monthly bang scheduler
  @Column(nullable = false, precision = 19, scale = 4)
  @Builder.Default
  BigDecimal dailyUsed = BigDecimal.ZERO;
  @Column(nullable = false, precision = 19, scale = 4)
  @Builder.Default
  BigDecimal monthlyUsed = BigDecimal.ZERO;
  LocalDateTime dailyResetAt;
  LocalDateTime monthlyResetAt;

}
