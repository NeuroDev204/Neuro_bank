package com.neuro_bank.module.user.entity;

import com.neuro_bank.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "otps", indexes = {
    @Index(name = "idx_otp_user_type", columnList = "user_id, type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Otp extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  User user;

  @Column(nullable = false, length = 255)
  String code;
  @Column(nullable = false, length = 30)
  String type;
  @Column(nullable = false)
  LocalDateTime expiresAt;
  @Column(nullable = false)
  @Builder.Default
  boolean used = false;
  LocalDateTime usedAt;
  @Column(length = 45)
  String ipAddress;

}
