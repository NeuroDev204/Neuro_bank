package com.neuro_bank.module.user.entity;

import com.neuro_bank.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token", indexes = {
    @Index(name = "idx_refresh_token_hash", columnList = "token_hash")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefreshToken extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, unique = true, length = 64)
  private String tokenHash;       // SHA-256 của raw token

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  @Column(nullable = false)
  private boolean revoked = false;

  private LocalDateTime revokedAt;

  @Column(nullable = false, length = 36)
  private String familyId;        // group các token cùng login session

  @Column(nullable = false)
  private int generation = 1;     // tăng mỗi lần rotate

  @Column(nullable = false, length = 36)
  private String sessionId;

  @Column(length = 45)
  private String ipAddress;

  @Column(length = 500)
  private String userAgent;

  @Column(length = 50)
  private String deviceId;
}

