package com.neuro_bank.module.loan.entity;

import com.neuro_bank.common.entity.BaseEntity;
import com.neuro_bank.module.transaction.entity.Transaction;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loan_payments")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoanPayment extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "loan_id", nullable = false)
  Loan loan;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "transaction_id")
  Transaction transaction;

  @Column(nullable = false, precision = 19, scale = 4)
  BigDecimal totalAmount;

  @Column(nullable = false, precision = 19, scale = 4)
  BigDecimal principalPart;

  @Column(nullable = false, precision = 19, scale = 4)
  BigDecimal interestPart;

  @Column(nullable = false, precision = 19, scale = 4)
  BigDecimal penaltyAmount = BigDecimal.ZERO;

  @Column(nullable = false)
  LocalDate dueDate;

  @Column(nullable = false)
  LocalDate paidDate;

  @Column(nullable = false)
  boolean onTime;
}