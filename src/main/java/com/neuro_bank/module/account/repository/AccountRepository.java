package com.neuro_bank.module.account.repository;

import com.neuro_bank.module.account.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
          select a from Account a
          left join fetch a.user
          left join fetch a.transactionLimit
          where a.id =:id and a.deleted = false
      """)
  Optional<Account> findByIdForUpdate(@Param("id") UUID uuid);

  Optional<Account> findByIdAndDeletedFalse(UUID uuid);
}
