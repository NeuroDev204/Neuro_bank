package com.neuro_bank.module.notification.service;

import com.neuro_bank.module.user.entity.User;

import java.util.UUID;

public interface AuditLogService {
  void log(User user, String action, String entityType, UUID entityId, boolean success, String ipAddress, String userAgent);

  void longAsync(User user, String action, String entityType, UUID entityId, boolean success, String ipAddress, String userAgent);
}
