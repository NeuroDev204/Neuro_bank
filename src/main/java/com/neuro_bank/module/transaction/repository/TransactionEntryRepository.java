package com.neuro_bank.module.transaction.repository;

import com.neuro_bank.module.transaction.entity.TransactionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionEntryRepository extends JpaRepository<TransactionEntry, UUID> {
  boolean existsByTransactionIdAndAccountUserId(UUID transactionId, UUID userId);
}
