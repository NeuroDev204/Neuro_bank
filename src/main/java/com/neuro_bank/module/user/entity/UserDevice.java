package com.neuro_bank.module.user.entity;


import com.neuro_bank.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_devices", indexes = {
    @Index(name = "idx_device_user", columnList = "user_id"),
    @Index(name = "idx_device_fingerprint", columnList = "device_fingerprint")
})
@Getter
@Setter
@NoArgsConstructor

public class UserDevice extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  //sha256 - useragent + screenResolution + timezone + platform
  @Column(nullable = false, length = 64)
  private String deviceFingerprint;
  @Column(nullable = false, length = 100)
  private String deviceName;
  @Column(length = 20)
  private String deviceType;
  @Column(nullable = false)
  private boolean trusted = false;

  private LocalDateTime trustedAt;
  @Column(length = 45)
  private String lastIpAddress;
  private LocalDateTime lastSeenAt;
  @Column(nullable = false)
  private int loginCount =0;
}
