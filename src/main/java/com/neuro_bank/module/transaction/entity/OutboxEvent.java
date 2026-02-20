package com.neuro_bank.module.transaction.entity;

import com.neuro_bank.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
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
@Table(name = "outbox_events", indexes = {
    @Index(name = "idx_outbox_processed", columnList = "processed, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OutboxEvent extends BaseEntity {

  @Column(nullable = false, length = 50)
  String eventType;  // TRANSACTION_COMPLETED, FRAUD_DETECTED...

  @Column(nullable = false, length = 50)
  String aggregateType;  // TRANSACTION, ACCOUNT...

  @Column(nullable = false)
  UUID aggregateId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  Map<String, Object> payload;

  @Column(nullable = false)
  boolean processed = false;

  LocalDateTime processedAt;

  int retryCount = 0;

  String lastError;
}
