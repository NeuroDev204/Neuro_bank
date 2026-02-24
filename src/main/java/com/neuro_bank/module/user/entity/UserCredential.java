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
  private User user;

  @Column(nullable = false)
  private String passwordHash; // Argon2id

  @Column(nullable = false)
  private String pinHash; // Argon2id với iterations cao hơn

  @Column(nullable = false)

  @Builder.Default
  private int failedLoginAttempts = 0;

  @Column(nullable = false)
  @Builder.Default
  private int failedPinAttempts = 0;

  private LocalDateTime lockedUntil;

  @Column(nullable = false)
  @Builder.Default
  private boolean twoFactorEnabled = false;

  private String twoFactorSecret; // TOTP secret — AES encrypted

  private LocalDateTime lastLoginAt;

  @Column(length = 45)
  private String lastLoginIp;
}
