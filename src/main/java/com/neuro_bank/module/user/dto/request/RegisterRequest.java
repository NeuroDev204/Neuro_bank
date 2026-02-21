package com.neuro_bank.module.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class RegisterRequest {
  @NotBlank(message = "Full name is required")
  @Size(min = 2, max = 100)
  private String fullName;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "Phone is required")
  @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Invalid Vietnamese phone number")
  private String phone;

  @NotBlank(message = "Password is required")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
      message = "Password must have uppercase, lowercase, number and special character"
  )
  private String password;

  @NotBlank(message = "PIN is required")
  @Pattern(regexp = "^\\d{6}$", message = "PIN must be 6 digits")
  private String pin;

  @NotNull(message = "Date of birth is required")
  @Past(message = "Date of birth must be in the past")
  private LocalDate dateOfBirth;

  @NotBlank(message = "National ID is required")
  @Size(min = 9, max = 12)
  private String nationalId;

  @Size(max = 255)
  private String address;
}
