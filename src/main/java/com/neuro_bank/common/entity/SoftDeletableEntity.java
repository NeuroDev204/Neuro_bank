package com.neuro_bank.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class SoftDeletableEntity extends BaseEntity {
  @Column(nullable = false)
  boolean deleted = false;
  LocalDateTime deletedAt;
  String deletedBy;
}
