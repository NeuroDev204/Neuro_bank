package com.neuro_bank.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
  private final int code;
  private final String errorKey;

  public BusinessException(String message){
    super(message);
    this.code = 400;
    this.errorKey ="BUSINESS_ERROR";
  }
  public BusinessException(int code, String message){
    super(message);
    this.code = code;
    this.errorKey = "BUSINESS_ERROR";
  }
  public BusinessException(int code, String errorKey,String message){
    super(message);
    this.code  = code;
    this.errorKey = errorKey;
  }

  public static BusinessException notFound(String resource) {
    return new BusinessException(404, "NOT_FOUND", resource + " not found");
  }

  public static BusinessException unauthorized(String message) {
    return new BusinessException(401, "UNAUTHORIZED", message);
  }

  public static BusinessException forbidden(String message) {
    return new BusinessException(403, "FORBIDDEN", message);
  }

  public static BusinessException conflict(String message) {
    return new BusinessException(409, "CONFLICT", message);
  }

  public static BusinessException badRequest(String message) {
    return new BusinessException(400, "BAD_REQUEST", message);
  }
}
