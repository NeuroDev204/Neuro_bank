package com.neuro_bank.module.user.repository;

import com.neuro_bank.module.user.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserCredentialRepository extends JpaRepository<UserCredential, UUID> {
}
