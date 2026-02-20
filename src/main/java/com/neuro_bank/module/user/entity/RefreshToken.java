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
  User user;

  @Column(nullable = false, unique = true, length = 512)
  String tokenHash;
  @Column(nullable = false)
  LocalDateTime expiresAt;
  String userAgent;
  @Column(nullable = false)
  @Builder.Default
  boolean revoked = false;

  LocalDateTime revokedAt;

  @Column(length = 45)
  String ipAddress;
  @Column(length = 255)
  String deviceInfo;
  @Column(length = 50)
  String deviceId;

}
