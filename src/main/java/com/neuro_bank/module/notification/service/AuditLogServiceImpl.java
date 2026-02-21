package com.neuro_bank.module.notification.service;

import com.neuro_bank.module.notification.entity.AuditLog;
import com.neuro_bank.module.notification.repository.AuditLogRepository;
import com.neuro_bank.module.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {
  private final AuditLogRepository auditLogRepository;

  @Override
  public void log(User user, String action, String entityType, UUID entityId, boolean success, String ipAddress, String userAgent) {
    try {
      AuditLog audit = new AuditLog();
      audit.setUser(user);
      audit.setAction(action);
      audit.setEntityType(entityType);
      audit.setEntityId(entityId);
      audit.setSuccess(success);
      audit.setIpAddress(ipAddress);
      audit.setUserAgent(userAgent);
      audit.setCreatedAt(LocalDateTime.now());
      auditLogRepository.save(audit);
    } catch (Exception e) {
      log.error("Failed to save audit log action={} user={}", action,
          user != null ? user.getId() : "null", e);
    }
  }

  @Override
  @Async("auditExecutor")
  public void longAsync(User user, String action, String entityType, UUID entityId, boolean success, String ipAddress, String userAgent) {
    log(user, action, entityType, entityId, success, ipAddress, userAgent);
  }
}
