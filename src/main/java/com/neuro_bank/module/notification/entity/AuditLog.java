package com.neuro_bank.module.notification.entity;

import com.neuro_bank.module.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_audit_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuditLog {  // Không extend BaseEntity, không có version, không update bao giờ

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  User user;

  @Column(nullable = false, length = 50)
  String action;  // LOGIN, TRANSFER, CHANGE_PASSWORD, LOCK_CARD...

  @Column(nullable = false, length = 50)
  String entityType;

  UUID entityId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  Map<String, Object> oldValue;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  Map<String, Object> newValue;

  @Column(length = 45)
  String ipAddress;

  @Column(length = 500)
  String userAgent;

  @Column(length = 50)
  String deviceId;

  @Column(nullable = false)
  LocalDateTime createdAt;

  @Column(nullable = false)
  boolean success;

  @Column(length = 500)
  String failureReason;
}