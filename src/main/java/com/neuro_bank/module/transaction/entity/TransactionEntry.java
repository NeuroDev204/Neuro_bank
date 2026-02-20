package com.neuro_bank.module.transaction.entity;

import com.neuro_bank.common.entity.BaseEntity;
import com.neuro_bank.common.enums.EntryType;
import com.neuro_bank.module.account.entity.Account;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "transaction_entries", indexes = {
    @Index(name = "idx_entry_account", columnList = "account_id"),
    @Index(name = "idx_entry_transaction", columnList = "transaction_id")
})
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionEntry extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "transaction_id", nullable = false)
  Transaction transaction;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  Account account;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  EntryType entryType;  // DEBIT hoặc CREDIT

  @Column(nullable = false, precision = 19, scale = 4)
  BigDecimal amount;

  // Balance tại thời điểm entry này — quan trọng cho audit
  @Column(nullable = false, precision = 19, scale = 4)
  BigDecimal balanceBefore;

  @Column(nullable = false, precision = 19, scale = 4)
  BigDecimal balanceAfter;
}
