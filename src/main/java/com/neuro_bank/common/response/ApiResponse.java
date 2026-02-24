package com.neuro_bank.common.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse<T> {
  private boolean success;
  private int code;
  private String message;
  private T data;

  public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder()
        .success(true)
        .code(200)
        .data(data)
        .build();
  }
  
  public static <T> ApiResponse<T> success(String message, T data) {
    return ApiResponse.<T>builder().success(true).code(200)
        .message(message).data(data).build();
  }
  public static <T> ApiResponse<T> error(int code, String message, String exMessage){
    return ApiResponse.<T>builder()
        .success(false)
        .code(code)
        .message(message)
        .build();
  }
}
