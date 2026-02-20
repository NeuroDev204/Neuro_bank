package com.neuro_bank.module.saving.entity;

import com.neuro_bank.common.entity.BaseEntity;
import com.neuro_bank.common.enums.SavingStatus;
import com.neuro_bank.module.account.entity.Account;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "saving_plans")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SavingPlan extends BaseEntity {

  @ManyToOne
      (fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  Account account;

  @Column(nullable = false, length = 100)
  String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  SavingStatus status = SavingStatus.ACTIVE;

  @Column(nullable = false, precision = 19, scale = 4)
  BigDecimal targetAmount;

  @Column(nullable = false, precision = 19, scale = 4)
  BigDecimal currentAmount = BigDecimal.ZERO;

  @Column(nullable = false, precision = 5, scale = 4)
  BigDecimal interestRate;

  @Column(nullable = false)
  LocalDate startDate;

  @Column(nullable = false)
  LocalDate maturityDate;

  // Tự động trừ từ account mỗi tháng
  @Column(nullable = false)
  boolean autoDebit = false;

  @Column(precision = 19, scale = 4)
  BigDecimal autoDebitAmount;

  int autoDebitDay; // ngày mấy trong tháng

  @Column(nullable = false, precision = 19, scale = 4)
  BigDecimal totalInterestEarned = BigDecimal.ZERO;

  LocalDateTime maturedAt;
  LocalDateTime withdrawnAt;
}