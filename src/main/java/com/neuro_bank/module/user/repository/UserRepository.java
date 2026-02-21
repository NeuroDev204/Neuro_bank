package com.neuro_bank.module.user.repository;

import com.neuro_bank.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
  // fetch credential cung luc de tranh lazyinit exception
  @Query("""
    SELECT u from User u
    LEFT JOIN FETCH u.credential
    where u.email = :email and u.deleted = false
""")
  Optional<User> findByEmailWithCredential(@Param("email") String email);

  @Query("""
    SELECT u FROM User u
    LEFT JOIN FETCH u.credential
    WHERE u.id = :id AND u.deleted = false
""")
  Optional<User> findByIdWithCredential(@Param("id") UUID id);

  boolean existsByEmail(String email);
  boolean existsByPhone(String phone);
  boolean existsByNationalId(String nationalId);

}
