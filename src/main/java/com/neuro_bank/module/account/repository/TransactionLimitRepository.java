package com.neuro_bank.module.account.repository;

import com.neuro_bank.module.account.entity.TransactionLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionLimitRepository extends JpaRepository<TransactionLimit, UUID> {
  Optional<TransactionLimit> findByAccountId(UUID accountId);
}
