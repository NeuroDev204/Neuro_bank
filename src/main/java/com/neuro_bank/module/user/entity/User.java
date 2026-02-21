package com.neuro_bank.module.user.entity;

import com.neuro_bank.common.entity.SoftDeletableEntity;
import com.neuro_bank.common.enums.KycStatus;
import com.neuro_bank.common.enums.UserStatus;
import com.neuro_bank.module.account.entity.Account;
import com.neuro_bank.module.notification.entity.AuditLog;
import com.neuro_bank.module.notification.entity.Notification;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email", unique = true),
    @Index(name = "idx_users_phone", columnList = "phone", unique = true),
    @Index(name = "idx_users_national_id", columnList = "national_id", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends SoftDeletableEntity {
  @Column(nullable = false)
  String fullName;
  @Column(nullable = false, unique = true, length = 100)
   String email;
  @Column(nullable = false, unique = true,length = 15)
  String phone;
  @Column(unique = true,length = 20)
  String nationalId; // CMND/CCCD
  @Column(nullable = false)
  LocalDate dateOfBirth;
  String address;
  String avatarUrl;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  @Builder.Default
  UserStatus status = UserStatus.PENDING_VERIFICATION;
  @Enumerated(EnumType.STRING)
  @Builder.Default
  KycStatus kycStatus = KycStatus.PENDING;
  LocalDateTime kycVerifiedAt;

  // Relationships
  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  UserCredential credential;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  List<Account> accounts = new ArrayList<>();

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  List<Notification> notifications = new ArrayList<>();

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  List<AuditLog> auditLogs = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<UserDevice> devices = new ArrayList<>();
}

