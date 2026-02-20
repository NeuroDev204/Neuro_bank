package com.neuro_bank.module.notification.entity;

import com.neuro_bank.common.entity.BaseEntity;
import com.neuro_bank.common.enums.NotificationChannel;
import com.neuro_bank.common.enums.NotificationType;
import com.neuro_bank.module.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notif_user_read", columnList = "user_id, is_read"),
    @Index(name = "idx_notif_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  User user;

  @Column(nullable = false, length = 200)
  String title;

  @Column(nullable = false, length = 1000)
  String body;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  NotificationType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  NotificationChannel channel;

  @Column(nullable = false)
  boolean isRead = false;

  LocalDateTime readAt;

  // Link tới entity liên quan
  UUID referenceId;

  @Column(length = 50)
  String referenceType;  // TRANSACTION, LOAN, CARD...

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  Map<String, Object> data;
}
