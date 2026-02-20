package com.neuro_bank.module.user.entity;

import com.neuro_bank.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_credentials")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCredential extends BaseEntity {
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  User user;

  @Column(nullable = false)
  String passwordHash;
  @Column(nullable = false)
  String pinHash; // 6-digit pin to confirm transaction
  @Column(nullable = false)
  @Builder.Default
  int failedPinAttempts = 0;
  LocalDateTime lockedUntil;
  @Column(nullable = false)
  @Builder.Default
  boolean towFactorEnabled = false;
  LocalDateTime lastLoginAt;
  @Column(length = 45)
  String lastLoginIp;
}
