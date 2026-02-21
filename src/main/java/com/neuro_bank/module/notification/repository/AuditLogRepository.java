package com.neuro_bank.module.notification.repository;

import com.neuro_bank.module.notification.entity.AuditLog;
import com.neuro_bank.module.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

  Page<AuditLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

  @Modifying
  @Query("DELETE FROM AuditLog  a WHERE a.createdAt <:before")
  void deleteOlderThan(@Param("before")LocalDateTime before);
}
