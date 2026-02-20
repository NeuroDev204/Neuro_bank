package com.neuro_bank.module.loan.entity;

import com.neuro_bank.common.entity.BaseEntity;
import com.neuro_bank.common.enums.LoanStatus;
import com.neuro_bank.module.account.entity.Account;
import com.neuro_bank.module.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Loan extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "disbursement_account_id")
  Account disbursementAccount;

  @Column(nullable = false, unique = true, length = 20)
  String loanNumber;

  @Column(nullable = false, precision = 19, scale = 4)
  BigDecimal principalAmount;

  @Column(nullable = false, precision = 5, scale = 4)
  BigDecimal annualInterestRate;

  @Column(nullable = false)
  int termMonths;

  @Column(nullable = false, precision = 19, scale = 4)
  BigDecimal monthlyPayment;

  @Column(nullable = false, precision = 19, scale = 4)
  BigDecimal outstandingPrincipal;

  @Column(nullable = false, precision = 19, scale = 4)
  BigDecimal totalInterestPaid = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  LoanStatus status = LoanStatus.PENDING_APPROVAL;

  @Column(length = 255)
  String purpose;

  LocalDateTime approvedAt;
  String approvedBy;
  LocalDateTime disbursedAt;
  LocalDate nextPaymentDue;
  LocalDateTime paidOffAt;

  @Column(length = 500)
  String rejectionReason;

  @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  List<LoanPayment> payments = new ArrayList<>();
}