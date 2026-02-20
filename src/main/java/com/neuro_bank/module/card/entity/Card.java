package com.neuro_bank.module.card.entity;

import com.neuro_bank.common.entity.SoftDeletableEntity;
import com.neuro_bank.common.enums.CardNetwork;
import com.neuro_bank.common.enums.CardStatus;
import com.neuro_bank.common.enums.CardType;
import com.neuro_bank.module.account.entity.Account;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(name = "cards", indexes = {
    @Index(name = "idx_cards_account", columnList = "account_id"),
    @Index(name = "idx_cards_last_four", columnList = "last_four_digits")
})
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Card extends SoftDeletableEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  Account account;

  // Encrypt AES-256, không bao giờ lưu raw
  @Column(nullable = false, length = 512)
  String cardNumberEncrypted;

  // Chỉ lưu 4 số cuối để hiển thị
  @Column(nullable = false, length = 4)
  String lastFourDigits;

  @Column(nullable = false, length = 512)
  String cvvHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  CardType cardType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  CardNetwork network;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  CardStatus status = CardStatus.PENDING;

  @Column(nullable = false)
  YearMonth expiryDate;

  @Column(nullable = false, length = 100)
  String cardHolderName;

  @Column(nullable = false, precision = 19, scale = 4)
  BigDecimal dailyLimit = new BigDecimal("20000000");

  // Chỉ cho credit card
  @Column(precision = 19, scale = 4)
  BigDecimal creditLimit;

  @Column(precision = 19, scale = 4)
  BigDecimal outstandingBalance;

  LocalDateTime activatedAt;
  LocalDateTime blockedAt;
  String blockedReason;

  @Column(nullable = false)
  boolean contactless = true;

  @Column(nullable = false)
  boolean internationalPayment = false;

  @Column(nullable = false)
  boolean onlineShopping = true;
}