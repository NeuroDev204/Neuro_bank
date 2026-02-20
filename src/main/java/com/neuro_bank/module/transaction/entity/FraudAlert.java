package com.neuro_bank.module.transaction.entity;

import com.neuro_bank.common.entity.BaseEntity;
import com.neuro_bank.module.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_alerts")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FraudAlert extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "transaction_id", nullable = false)
  Transaction transaction;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  User user;

  @Column(nullable = false, length = 100)
  String ruleTriggered;  // RATE_LIMIT_EXCEEDED, UNUSUAL_AMOUNT...

  @Column(nullable = false, length = 20)
  String severity;  // LOW, MEDIUM, HIGH, CRITICAL

  @Column(nullable = false)
  boolean resolved = false;

  LocalDateTime resolvedAt;
  String resolvedBy;

  @Column(length = 500)
  String notes;
}