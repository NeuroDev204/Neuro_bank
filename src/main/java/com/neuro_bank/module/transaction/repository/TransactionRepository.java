package com.neuro_bank.module.transaction.repository;

import com.neuro_bank.common.enums.TransactionStatus;
import com.neuro_bank.common.enums.TransactionType;
import com.neuro_bank.module.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
  Optional<Transaction> findByReferenceNo(String referenceNo);

  Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

  @EntityGraph(attributePaths = {"entries", "entries.account", "parentTransaction"})
  @Query("SELECT t FROM Transaction t WHERE t.referenceNo=:referenceNo")
  Optional<Transaction> findWithEntriesByReferenceNo(@Param("referenceNo") String referenceNo);

  @Query("""
          select distinct t
          from Transaction  t
          join t.entries e
          join e.account a
          where a.user.id =:userId
          and (:status is null or t.status =:status)
          and (:type is null or t.type =:type)
          and (:fromDate is null or t.createdAt >=:fromDate)
          and (:toDate is null or t.createdAt <= :toDate)
      """)
  Page<Transaction> searchMyTransactions(@Param("userId") UUID userId,
                                         @Param("status") TransactionStatus status,
                                         @Param("type") TransactionType type,
                                         @Param("fromDate") LocalDateTime fromDate,
                                         @Param("toDate") LocalDateTime toDate,
                                         Pageable pageable);
}
