package com.neuro_bank.module.user.repository;

import com.neuro_bank.module.user.entity.User;
import com.neuro_bank.module.user.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, UUID> {
  Optional<UserDevice> findByUserAndDeviceFingerprint(User user, String fingerprint);

  List<UserDevice> findByUserOrderByLastSeenAtDesc(User user);
}
