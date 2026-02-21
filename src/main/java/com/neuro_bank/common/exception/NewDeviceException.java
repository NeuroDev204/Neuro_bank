package com.neuro_bank.common.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class NewDeviceException extends RuntimeException {
  private final UUID userId;
  public NewDeviceException(UUID userId, String message){
    super(message);
    this.userId = userId;
  }

}
