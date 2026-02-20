package com.neuro_bank.module.account.entity;


import com.neuro_bank.common.entity.SoftDeletableEntity;
import com.neuro_bank.common.enums.AccountStatus;
import com.neuro_bank.common.enums.AccountType;
import com.neuro_bank.common.enums.Currency;
import com.neuro_bank.module.card.entity.Card;
import com.neuro_bank.module.saving.entity.SavingPlan;
import com.neuro_bank.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts",indexes = {
    @Index(name = "idx_accounts_number", columnList = "account_number", unique = true),
    @Index(name = "idx_accounts_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Account extends SoftDeletableEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id",nullable = false)
  User user;
  @Column(nullable = false, unique = true, length = 20)
  String accountNumber;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  AccountType type;
  @Enumerated(EnumType.STRING)
  AccountStatus status;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)

  Currency currency = Currency.VND;

  // dung BigDecimal cho tien
  @Column(nullable = false, precision = 19, scale = 4)
  @Builder.Default
  BigDecimal balance = BigDecimal.ZERO;
  // balance tru cac pending transaction
  @Column(nullable = false,precision = 19,scale = 4)
  @Builder.Default
  BigDecimal availableBalance = BigDecimal.ZERO;
  // interest rate cho saving account
  @Column(precision = 5, scale = 4)
  BigDecimal interestRate;

  LocalDateTime frozenAt;
  String frozenReason;
  @Column(nullable = false)
  LocalDate openedDate;
  LocalDate closedDate;
  // Relationships
  @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
  TransactionLimit transactionLimit;

  @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
  List<Card> cards = new ArrayList<>();

  @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
  List<SavingPlan> savingPlans = new ArrayList<>();
}
