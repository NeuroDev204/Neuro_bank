package com.neuro_bank.module.user.repository;

import com.neuro_bank.module.user.entity.Otp;
import com.neuro_bank.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface OtpRepository extends JpaRepository<Otp, UUID> {

  @Query("""
          SELECT o FROM Otp o
          WHERE o.user =:user AND o.type =:type
          AND o.code =:codeHash AND o.used = false
          AND o.expiresAt >:now
          ORDER BY o.createdAt DESC LIMIT 1
      """)
  Otp findValidOtp(@Param("user") User user, @Param("type") String type,
                   @Param("codeHash") String codeHash, @Param("now") LocalDateTime now);

  @Modifying
  @Query("UPDATE Otp o SET o.used = true, o.updatedAt =:now WHERE o.user =:user and o.type =:type and o.used = false")
  void invalidateExisting(@Param("user") User user, @Param("type") String type, @Param("now") LocalDateTime now);
}
