package com.neuro_bank.module.transaction.entity;

import com.neuro_bank.common.entity.BaseEntity;
import com.neuro_bank.common.enums.Currency;
import com.neuro_bank.common.enums.TransactionStatus;
import com.neuro_bank.common.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_txn_reference", columnList = "reference_no", unique = true),
    @Index(name = "idx_txn_status", columnList = "status"),
    @Index(name = "idx_txn_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Transaction extends BaseEntity {
  @Column(nullable = false, unique = true, length = 30)
  String referenceNo;  // TXN20240119XXXXXXXX

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  TransactionType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  TransactionStatus status = TransactionStatus.PENDING;

  @Column(nullable = false, precision = 19, scale = 4)
  BigDecimal amount;

  @Column(precision = 19, scale = 4)
  BigDecimal fee = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  Currency currency;

  @Column(length = 255)
  String description;

  // Idempotency key từ client
  @Column(unique = true, length = 64)
  String idempotencyKey;

  LocalDateTime completedAt;
  LocalDateTime reversedAt;

  // Reference tới transaction gốc nếu đây là refund/reversal
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_transaction_id")
  Transaction parentTransaction;
  // Metadata: lưu thêm info (bank code, channel, etc) dạng JSON

  @Column(columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  Map<String, Object> metadata = new HashMap<>();

  // Double-entry entries
  @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  List<TransactionEntry> entries = new ArrayList<>();

  @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL)
  FraudAlert fraudAlert;
  // Helper
  public BigDecimal getTotalAmount() {
    return amount.add(fee);
  }
}
