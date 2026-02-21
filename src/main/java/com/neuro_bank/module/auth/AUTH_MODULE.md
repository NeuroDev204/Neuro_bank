# Banking Auth Module — Toàn bộ file hệ thống

## Cấu trúc thư mục
```
src/main/java/com/banking/
├── BankingApplication.java
├── config/
│   ├── AsyncConfig.java
│   ├── AuditConfig.java
│   └── SecurityConfig.java
├── common/
│   ├── entity/
│   │   ├── BaseEntity.java
│   │   └── SoftDeletableEntity.java
│   ├── enums/
│   │   ├── KycStatus.java
│   │   └── UserStatus.java
│   ├── exception/
│   │   ├── BusinessException.java
│   │   ├── NewDeviceException.java
│   │   └── GlobalExceptionHandler.java
│   └── response/
│       └── ApiResponse.java
├── security/
│   ├── CustomJwtAuthenticationConverter.java
│   ├── CustomUserDetailsService.java
│   ├── JwtTokenProvider.java
│   ├── SecurityAuditorAware.java
│   └── UserPrincipal.java
├── infrastructure/
│   ├── filter/
│   │   └── RateLimitFilter.java
│   └── redis/
│       ├── RedisService.java
│       └── RateLimiterService.java
├── module/
│   ├── auth/
│   │   ├── controller/AuthController.java
│   │   ├── dto/request/
│   │   │   ├── LoginRequest.java
│   │   │   └── VerifyNewDeviceRequest.java
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── AuthServiceImpl.java
│   │   │   ├── LoginResult.java
│   │   │   └── RefreshResult.java
│   │   └── util/CookieUtil.java
│   ├── user/
│   │   ├── dto/request/
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   ├── UpdateProfileRequest.java
│   │   │   └── VerifyOtpRequest.java
│   │   ├── dto/response/UserResponse.java
│   │   ├── entity/
│   │   │   ├── Otp.java
│   │   │   ├── RefreshToken.java
│   │   │   ├── User.java
│   │   │   ├── UserCredential.java
│   │   │   └── UserDevice.java
│   │   ├── repository/
│   │   │   ├── OtpRepository.java
│   │   │   ├── RefreshTokenRepository.java
│   │   │   ├── UserCredentialRepository.java
│   │   │   ├── UserDeviceRepository.java
│   │   │   └── UserRepository.java
│   │   └── service/
│   │       ├── OtpService.java
│   │       ├── OtpServiceImpl.java
│   │       ├── UserService.java
│   │       └── UserServiceImpl.java
│   └── notification/
│       ├── entity/AuditLog.java
│       ├── repository/AuditLogRepository.java
│       └── service/
│           ├── AuditLogService.java
│           └── AuditLogServiceImpl.java
└── scheduler/
    └── RefreshTokenCleanupScheduler.java

src/main/resources/
├── certs/
│   ├── private.pem
│   └── public.pem
├── db/migration/
│   └── V1__init_auth.sql
├── application.yml
├── application-dev.yml
└── application-prod.yml
```

---

## 1. BankingApplication.java
```java
package com.banking;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BankingApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankingApplication.class, args);
    }
}
```

---

## 2. common/enums/UserStatus.java
```java
package com.banking.common.enums;
public enum UserStatus {
    PENDING_VERIFICATION, ACTIVE, SUSPENDED, CLOSED
}
```

---

## 3. common/enums/KycStatus.java
```java
package com.banking.common.enums;
public enum KycStatus {
    PENDING, VERIFIED, REJECTED
}
```

---

## 4. common/entity/BaseEntity.java
```java
package com.banking.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false, length = 50)
    private String createdBy;

    @LastModifiedBy
    @Column(length = 50)
    private String updatedBy;

    @Version
    private Long version;
}
```

---

## 5. common/entity/SoftDeletableEntity.java
```java
package com.banking.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter @Setter
public abstract class SoftDeletableEntity extends BaseEntity {

    @Column(nullable = false)
    private boolean deleted = false;

    private LocalDateTime deletedAt;

    @Column(length = 50)
    private String deletedBy;
}
```

---

## 6. common/exception/BusinessException.java
```java
package com.banking.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;
    private final String errorKey;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
        this.errorKey = "BUSINESS_ERROR";
    }

    public BusinessException(int code, String errorKey, String message) {
        super(message);
        this.code = code;
        this.errorKey = errorKey;
    }

    public static BusinessException notFound(String resource) {
        return new BusinessException(404, "NOT_FOUND", resource + " not found");
    }
    public static BusinessException conflict(String message) {
        return new BusinessException(409, "CONFLICT", message);
    }
    public static BusinessException badRequest(String message) {
        return new BusinessException(400, "BAD_REQUEST", message);
    }
    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, "UNAUTHORIZED", message);
    }
    public static BusinessException forbidden(String message) {
        return new BusinessException(403, "FORBIDDEN", message);
    }
    public static BusinessException tooManyRequests(String message) {
        return new BusinessException(429, "TOO_MANY_REQUESTS", message);
    }
}
```

---

## 7. common/exception/NewDeviceException.java
```java
package com.banking.common.exception;

import lombok.Getter;
import java.util.UUID;

@Getter
public class NewDeviceException extends RuntimeException {
    private final UUID userId;

    public NewDeviceException(UUID userId, String message) {
        super(message);
        this.userId = userId;
    }
}
```

---

## 8. common/exception/GlobalExceptionHandler.java
```java
package com.banking.common.exception;

import com.banking.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(ex.getCode())
                .body(ApiResponse.error(ex.getCode(), ex.getErrorKey(), ex.getMessage()));
    }

    @ExceptionHandler(NewDeviceException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleNewDevice(NewDeviceException ex) {
        Map<String, String> data = Map.of("userId", ex.getUserId().toString());
        return ResponseEntity.status(202)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false).code(202)
                        .errorKey("NEW_DEVICE_VERIFICATION_REQUIRED")
                        .message(ex.getMessage())
                        .data(data).build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false).code(400).errorKey("VALIDATION_ERROR")
                        .message("Validation failed").data(errors).build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied() {
        return ResponseEntity.status(403)
                .body(ApiResponse.error(403, "FORBIDDEN", "Access denied"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        log.error("Unhandled exception: ", ex);
        return ResponseEntity.status(500)
                .body(ApiResponse.error(500, "INTERNAL_ERROR", "Internal server error"));
    }
}
```

---

## 9. common/response/ApiResponse.java
```java
package com.banking.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private int code;
    private String errorKey;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder().success(true).code(200).data(data).build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder().success(true).code(200)
                .message(message).data(data).build();
    }

    public static <T> ApiResponse<T> error(int code, String errorKey, String message) {
        return ApiResponse.<T>builder().success(false).code(code)
                .errorKey(errorKey).message(message).build();
    }
}
```

---

## 10. module/user/entity/User.java
```java
package com.banking.module.user.entity;

import com.banking.common.entity.SoftDeletableEntity;
import com.banking.common.enums.KycStatus;
import com.banking.common.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email", unique = true),
    @Index(name = "idx_users_phone", columnList = "phone", unique = true),
    @Index(name = "idx_users_national_id", columnList = "national_id", unique = true)
})
@Getter @Setter @NoArgsConstructor
public class User extends SoftDeletableEntity {

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, unique = true, length = 15)
    private String phone;

    @Column(unique = true, length = 20)
    private String nationalId;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(length = 255)
    private String address;

    @Column(length = 255)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KycStatus kycStatus = KycStatus.PENDING;

    private LocalDateTime kycVerifiedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserCredential credential;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserDevice> devices = new ArrayList<>();
}
```

---

## 11. module/user/entity/UserCredential.java
```java
package com.banking.module.user.entity;

import com.banking.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_credentials")
@Getter @Setter @NoArgsConstructor
public class UserCredential extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String passwordHash;   // Argon2id

    @Column(nullable = false)
    private String pinHash;        // Argon2id với iterations cao hơn

    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    @Column(nullable = false)
    private int failedPinAttempts = 0;

    private LocalDateTime lockedUntil;

    @Column(nullable = false)
    private boolean twoFactorEnabled = false;

    private String twoFactorSecret; // TOTP secret — AES encrypted

    private LocalDateTime lastLoginAt;

    @Column(length = 45)
    private String lastLoginIp;
}
```

---

## 12. module/user/entity/RefreshToken.java
```java
package com.banking.module.user.entity;

import com.banking.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_token_hash", columnList = "token_hash", unique = true),
    @Index(name = "idx_refresh_family", columnList = "family_id")
})
@Getter @Setter @NoArgsConstructor
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;       // SHA-256 của raw token

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    private LocalDateTime revokedAt;

    @Column(nullable = false, length = 36)
    private String familyId;        // group các token cùng login session

    @Column(nullable = false)
    private int generation = 1;     // tăng mỗi lần rotate

    @Column(nullable = false, length = 36)
    private String sessionId;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(length = 50)
    private String deviceId;
}
```

---

## 13. module/user/entity/Otp.java
```java
package com.banking.module.user.entity;

import com.banking.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otps", indexes = {
    @Index(name = "idx_otp_user_type", columnList = "user_id, type, used")
})
@Getter @Setter @NoArgsConstructor
public class Otp extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 64)
    private String codeHash;    // SHA-256 — không lưu plaintext

    // EMAIL_VERIFICATION | NEW_DEVICE_LOGIN | RESET_PASSWORD | TRANSACTION
    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    private LocalDateTime usedAt;

    @Column(length = 45)
    private String ipAddress;
}
```

---

## 14. module/user/entity/UserDevice.java
```java
package com.banking.module.user.entity;

import com.banking.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_devices", indexes = {
    @Index(name = "idx_device_user", columnList = "user_id"),
    @Index(name = "idx_device_fingerprint", columnList = "device_fingerprint")
})
@Getter @Setter @NoArgsConstructor
public class UserDevice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // SHA-256(userAgent + screenResolution + timezone + platform) — tính từ client
    @Column(nullable = false, length = 64)
    private String deviceFingerprint;

    @Column(nullable = false, length = 100)
    private String deviceName;

    @Column(length = 20)
    private String deviceType;      // MOBILE | DESKTOP | TABLET

    @Column(nullable = false)
    private boolean trusted = false;

    private LocalDateTime trustedAt;

    @Column(length = 45)
    private String lastIpAddress;

    private LocalDateTime lastSeenAt;

    @Column(nullable = false)
    private int loginCount = 0;
}
```

---

## 15. module/notification/entity/AuditLog.java
```java
package com.banking.module.notification.entity;

import com.banking.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_created", columnList = "created_at")
})
@Getter @Setter @NoArgsConstructor
public class AuditLog {

    // Không extend BaseEntity — immutable, không bao giờ update
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(nullable = false, length = 50)
    private String entityType;

    private UUID entityId;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(nullable = false)
    private boolean success;

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
```

---

## 16. module/user/repository/UserRepository.java
```java
package com.banking.module.user.repository;

import com.banking.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.credential WHERE u.email = :email AND u.deleted = false")
    Optional<User> findByEmailWithCredential(@Param("email") String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.credential WHERE u.id = :id AND u.deleted = false")
    Optional<User> findByIdWithCredential(@Param("id") UUID id);

    Optional<User> findByEmailAndDeletedFalse(String email);

    boolean existsByEmailAndDeletedFalse(String email);
    boolean existsByPhoneAndDeletedFalse(String phone);
    boolean existsByNationalIdAndDeletedFalse(String nationalId);
}
```

---

## 17. module/user/repository/UserCredentialRepository.java
```java
package com.banking.module.user.repository;

import com.banking.module.user.entity.User;
import com.banking.module.user.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserCredentialRepository extends JpaRepository<UserCredential, UUID> {
    Optional<UserCredential> findByUser(User user);
}
```

---

## 18. module/user/repository/RefreshTokenRepository.java
```java
package com.banking.module.user.repository;

import com.banking.module.user.entity.RefreshToken;
import com.banking.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :now WHERE r.familyId = :familyId AND r.revoked = false")
    void revokeByFamilyId(@Param("familyId") String familyId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :now WHERE r.user = :user AND r.revoked = false")
    void revokeAllByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now OR r.revoked = true")
    void deleteExpiredAndRevoked(@Param("now") LocalDateTime now);
}
```

---

## 19. module/user/repository/OtpRepository.java
```java
package com.banking.module.user.repository;

import com.banking.module.user.entity.Otp;
import com.banking.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OtpRepository extends JpaRepository<Otp, UUID> {

    @Query("""
        SELECT o FROM Otp o
        WHERE o.user = :user AND o.type = :type
          AND o.codeHash = :codeHash AND o.used = false
          AND o.expiresAt > :now
        ORDER BY o.createdAt DESC LIMIT 1
    """)
    Optional<Otp> findValidOtp(@Param("user") User user, @Param("type") String type,
                                @Param("codeHash") String codeHash, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Otp o SET o.used = true, o.usedAt = :now WHERE o.user = :user AND o.type = :type AND o.used = false")
    void invalidateExisting(@Param("user") User user, @Param("type") String type, @Param("now") LocalDateTime now);
}
```

---

## 20. module/user/repository/UserDeviceRepository.java
```java
package com.banking.module.user.repository;

import com.banking.module.user.entity.User;
import com.banking.module.user.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserDeviceRepository extends JpaRepository<UserDevice, UUID> {
    Optional<UserDevice> findByUserAndDeviceFingerprint(User user, String fingerprint);
    List<UserDevice> findByUserOrderByLastSeenAtDesc(User user);
}
```

---

## 21. module/notification/repository/AuditLogRepository.java
```java
package com.banking.module.notification.repository;

import com.banking.module.notification.entity.AuditLog;
import com.banking.module.user.entity.User;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Page<AuditLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.createdAt < :before")
    void deleteOlderThan(@Param("before") LocalDateTime before);
}
```

---

## 22. module/user/dto/request/RegisterRequest.java
```java
package com.banking.module.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter
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
```

---

## 23. module/user/dto/request/VerifyOtpRequest.java
```java
package com.banking.module.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
public class VerifyOtpRequest {

    @NotBlank @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
    private String code;
}
```

---

## 24. module/user/dto/request/UpdateProfileRequest.java
```java
package com.banking.module.user.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
public class UpdateProfileRequest {

    @Size(min = 2, max = 100)
    private String fullName;

    @Size(max = 255)
    private String address;

    @Size(max = 255)
    private String avatarUrl;
}
```

---

## 25. module/auth/dto/request/LoginRequest.java
```java
package com.banking.module.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
public class LoginRequest {

    @NotBlank @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    // SHA-256(userAgent + screenResolution + timezone + platform) — tính từ client
    @Size(max = 64)
    private String deviceFingerprint;
}
```

---

## 26. module/auth/dto/request/VerifyNewDeviceRequest.java
```java
package com.banking.module.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;

@Getter @Setter
public class VerifyNewDeviceRequest {

    @NotNull
    private UUID userId;

    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
    private String otpCode;
}
```

---

## 27. module/user/dto/response/UserResponse.java
```java
package com.banking.module.user.dto.response;

import com.banking.common.enums.*;
import com.banking.module.user.entity.User;
import com.banking.security.UserPrincipal;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.*;
import java.util.UUID;

@Getter @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private String nationalId;
    private LocalDate dateOfBirth;
    private String address;
    private String avatarUrl;
    private UserStatus status;
    private KycStatus kycStatus;
    private LocalDateTime kycVerifiedAt;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId()).fullName(user.getFullName())
                .email(user.getEmail()).phone(user.getPhone())
                .nationalId(user.getNationalId()).dateOfBirth(user.getDateOfBirth())
                .address(user.getAddress()).avatarUrl(user.getAvatarUrl())
                .status(user.getStatus()).kycStatus(user.getKycStatus())
                .kycVerifiedAt(user.getKycVerifiedAt()).createdAt(user.getCreatedAt())
                .build();
    }

    public static UserResponse from(UserPrincipal principal) {
        return UserResponse.builder()
                .id(principal.getId()).email(principal.getEmail())
                .phone(principal.getPhone()).status(principal.getStatus())
                .build();
    }
}
```

---

## 28. module/auth/service/LoginResult.java
```java
package com.banking.module.auth.service;

import com.banking.module.user.dto.response.UserResponse;

public record LoginResult(
    String accessToken,
    String refreshToken,
    UserResponse userResponse
) {}
```

---

## 29. module/auth/service/RefreshResult.java
```java
package com.banking.module.auth.service;

public record RefreshResult(
    String newAccessToken,
    String newRefreshToken
) {}
```

---

## 30. infrastructure/redis/RedisService.java
```java
package com.banking.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public void set(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public boolean setIfAbsent(String key, String value, Duration ttl) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, ttl));
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public long increment(String key, Duration ttl) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, ttl);
        }
        return count != null ? count : 0;
    }

    public void blacklistToken(String jti, Duration ttl) {
        set("blacklist:jti:" + jti, "1", ttl);
    }

    public boolean isTokenBlacklisted(String jti) {
        return hasKey("blacklist:jti:" + jti);
    }
}
```

---

## 31. infrastructure/redis/RateLimiterService.java
```java
package com.banking.infrastructure.redis;

import com.banking.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisService redisService;

    public void checkRateLimit(String key, int maxRequests, Duration window) {
        long count = redisService.increment(key, window);
        if (count > maxRequests) {
            throw BusinessException.tooManyRequests("Too many requests. Please try again later");
        }
    }

    public void checkLoginRateLimitByIp(String ip) {
        checkRateLimit("rate:login:ip:" + ip, 10, Duration.ofMinutes(15));
    }

    public void checkLoginRateLimitByEmail(String email) {
        // Hash email để không lưu PII trong Redis key
        checkRateLimit("rate:login:email:" + DigestUtils.sha256Hex(email), 5, Duration.ofMinutes(15));
    }

    public void checkOtpSendRateLimit(UUID userId, String type) {
        checkRateLimit("rate:otp:send:" + userId + ":" + type, 3, Duration.ofMinutes(10));
    }
}
```

---

## 32. infrastructure/filter/RateLimitFilter.java
```java
package com.banking.infrastructure.filter;

import com.banking.common.exception.BusinessException;
import com.banking.infrastructure.redis.RateLimiterService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String ip = getClientIp(request);
        try {
            rateLimiterService.checkRateLimit("rate:global:" + ip, 100, Duration.ofMinutes(1));
            filterChain.doFilter(request, response);
        } catch (BusinessException ex) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "60");
            response.getWriter().write(
                "{\"success\":false,\"code\":429,\"errorKey\":\"TOO_MANY_REQUESTS\",\"message\":\"Too many requests\"}");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank())
            return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
```

---

## 33. security/UserPrincipal.java
```java
package com.banking.security;

import com.banking.common.enums.UserStatus;
import com.banking.module.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.*;

@Getter
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String phone;
    private final String password;
    private final UserStatus status;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.password = user.getCredential().getPasswordHash();
        this.status = user.getStatus();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return status != UserStatus.SUSPENDED; }
    @Override public boolean isEnabled() { return status == UserStatus.ACTIVE; }
}
```

---

## 34. security/CustomUserDetailsService.java
```java
package com.banking.security;

import com.banking.module.user.entity.User;
import com.banking.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithCredential(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new UserPrincipal(user);
    }

    public UserDetails loadUserById(UUID id) {
        User user = userRepository.findByIdWithCredential(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));
        return new UserPrincipal(user);
    }
}
```

---

## 35. security/JwtTokenProvider.java
```java
package com.banking.security;

import com.banking.common.exception.BusinessException;
import com.banking.module.user.entity.User;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.interfaces.*;
import java.time.*;
import java.util.*;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.issuer}")
    private String issuer;

    @Value("${app.jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Value("${app.jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    private final RSAKey rsaKey;

    public JwtTokenProvider(
            @Value("${app.jwt.private-key-location}") RSAPrivateKey privateKey,
            @Value("${spring.security.oauth2.resourceserver.jwt.public-key-location}") RSAPublicKey publicKey) {
        this.rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }

    public String generateAccessToken(User user, String deviceFingerprint, String sessionId) {
        return buildToken(user, accessTokenExpiry, "ACCESS", deviceFingerprint, sessionId);
    }

    public String generateRefreshToken(User user, String deviceFingerprint, String sessionId) {
        return buildToken(user, refreshTokenExpiry, "REFRESH", deviceFingerprint, sessionId);
    }

    private String buildToken(User user, long expirySeconds, String tokenType,
                               String deviceFingerprint, String sessionId) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getId().toString())
                    .issuer(issuer)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(expirySeconds)))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("email", user.getEmail())
                    .claim("status", user.getStatus().name())
                    .claim("tokenType", tokenType)
                    .claim("sessionId", sessionId)
                    .claim("deviceFingerprint", deviceFingerprint)
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                    claims);
            signedJWT.sign(new RSASSASigner(rsaKey));
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Cannot generate JWT", e);
        }
    }

    public JWTClaimsSet parseToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!jwt.verify(new RSASSAVerifier(rsaKey.toRSAPublicKey())))
                throw BusinessException.unauthorized("Invalid token signature");

            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (claims.getExpirationTime().before(new Date()))
                throw BusinessException.unauthorized("Token expired");

            return claims;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw BusinessException.unauthorized("Invalid token");
        }
    }

    public String extractJti(String token) {
        try { return SignedJWT.parse(token).getJWTClaimsSet().getJWTID(); }
        catch (Exception e) { return null; }
    }

    public Duration getRemainingTtl(String token) {
        try {
            Date expiry = SignedJWT.parse(token).getJWTClaimsSet().getExpirationTime();
            long seconds = (expiry.getTime() - System.currentTimeMillis()) / 1000;
            return Duration.ofSeconds(Math.max(seconds, 0));
        } catch (Exception e) { return Duration.ZERO; }
    }
}
```

---

## 36. security/CustomJwtAuthenticationConverter.java
```java
package com.banking.security;

import com.banking.infrastructure.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    private final CustomUserDetailsService userDetailsService;
    private final RedisService redisService;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // 1. Chỉ ACCESS token được dùng cho API
        String tokenType = jwt.getClaimAsString("tokenType");
        if (!"ACCESS".equals(tokenType))
            throw new BadCredentialsException("Invalid token type");

        // 2. Check blacklist — token đã logout
        String jti = jwt.getId();
        if (jti != null && redisService.isTokenBlacklisted(jti))
            throw new BadCredentialsException("Token has been revoked");

        // 3. Load user để lấy status mới nhất từ DB
        UUID userId = UUID.fromString(jwt.getSubject());
        UserDetails userDetails = userDetailsService.loadUserById(userId);

        return new UsernamePasswordAuthenticationToken(
                userDetails, jwt, userDetails.getAuthorities());
    }
}
```

---

## 37. security/SecurityAuditorAware.java
```java
package com.banking.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class SecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal()))
            return Optional.of("SYSTEM");
        if (auth.getPrincipal() instanceof UserPrincipal principal)
            return Optional.of(principal.getId().toString());
        return Optional.of("SYSTEM");
    }
}
```

---

## 38. module/notification/service/AuditLogService.java
```java
package com.banking.module.notification.service;

import com.banking.module.user.entity.User;
import java.util.UUID;

public interface AuditLogService {
    void log(User user, String action, String entityType, UUID entityId,
             boolean success, String ipAddress, String userAgent);

    void logAsync(User user, String action, String entityType, UUID entityId,
                  boolean success, String ipAddress, String userAgent);
}
```

---

## 39. module/notification/service/AuditLogServiceImpl.java
```java
package com.banking.module.notification.service;

import com.banking.module.notification.entity.AuditLog;
import com.banking.module.notification.repository.AuditLogRepository;
import com.banking.module.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User user, String action, String entityType, UUID entityId,
                    boolean success, String ipAddress, String userAgent) {
        try {
            AuditLog audit = new AuditLog();
            audit.setUser(user);
            audit.setAction(action);
            audit.setEntityType(entityType);
            audit.setEntityId(entityId);
            audit.setSuccess(success);
            audit.setIpAddress(ipAddress);
            audit.setUserAgent(userAgent);
            audit.setCreatedAt(LocalDateTime.now());
            auditLogRepository.save(audit);
        } catch (Exception e) {
            log.error("Failed to save audit log action={} user={}", action,
                    user != null ? user.getId() : "null", e);
        }
    }

    @Override
    @Async("auditExecutor")
    public void logAsync(User user, String action, String entityType, UUID entityId,
                         boolean success, String ipAddress, String userAgent) {
        log(user, action, entityType, entityId, success, ipAddress, userAgent);
    }
}
```

---

## 40. module/user/service/OtpService.java
```java
package com.banking.module.user.service;

import com.banking.module.user.entity.User;

public interface OtpService {
    void sendOtp(User user, String type);
    void verifyOtp(User user, String type, String code);
    void invalidateOtp(User user, String type);
}
```

---

## 41. module/user/service/OtpServiceImpl.java
```java
package com.banking.module.user.service;

import com.banking.common.exception.BusinessException;
import com.banking.infrastructure.redis.*;
import com.banking.module.user.entity.*;
import com.banking.module.user.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final RedisService redisService;
    private final RateLimiterService rateLimiterService;

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    @Override
    @Transactional
    public void sendOtp(User user, String type) {
        rateLimiterService.checkOtpSendRateLimit(user.getId(), type);

        otpRepository.invalidateExisting(user, type, LocalDateTime.now());

        String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        String codeHash = DigestUtils.sha256Hex(code); // hash trước khi lưu

        Otp otp = new Otp();
        otp.setUser(user);
        otp.setCodeHash(codeHash);
        otp.setType(type);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        otpRepository.save(otp);

        log.info("[DEV-OTP] user={} type={} code={}", user.getEmail(), type, code);
        // PROD: emailService.sendOtp(user.getEmail(), code);
    }

    @Override
    @Transactional
    public void verifyOtp(User user, String type, String code) {
        String attemptKey = "otp:attempt:" + user.getId() + ":" + type;
        long attempts = redisService.increment(attemptKey, Duration.ofMinutes(OTP_EXPIRY_MINUTES));

        if (attempts > MAX_VERIFY_ATTEMPTS)
            throw BusinessException.tooManyRequests("Too many failed attempts. Please request a new OTP");

        String codeHash = DigestUtils.sha256Hex(code);

        Otp otp = otpRepository.findValidOtp(user, type, codeHash, LocalDateTime.now())
                .orElseThrow(() -> BusinessException.badRequest("Invalid or expired OTP"));

        otp.setUsed(true);
        otp.setUsedAt(LocalDateTime.now());
        otpRepository.save(otp);

        redisService.delete(attemptKey);
    }

    @Override
    @Transactional
    public void invalidateOtp(User user, String type) {
        otpRepository.invalidateExisting(user, type, LocalDateTime.now());
    }
}
```

---

## 42. module/user/service/UserService.java
```java
package com.banking.module.user.service;

import com.banking.module.user.dto.request.*;
import com.banking.module.user.dto.response.UserResponse;
import java.util.UUID;

public interface UserService {
    UserResponse register(RegisterRequest request);
    void verifyEmail(VerifyOtpRequest request);
    void resendOtp(String email);
    UserResponse getById(UUID id);
    UserResponse updateProfile(UUID userId, UpdateProfileRequest request);
}
```

---

## 43. module/user/service/UserServiceImpl.java
```java
package com.banking.module.user.service;

import com.banking.common.enums.*;
import com.banking.common.exception.BusinessException;
import com.banking.module.notification.service.AuditLogService;
import com.banking.module.user.dto.request.*;
import com.banking.module.user.dto.response.UserResponse;
import com.banking.module.user.entity.*;
import com.banking.module.user.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserCredentialRepository credentialRepository;
    private final OtpService otpService;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordEncoder pinEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           UserCredentialRepository credentialRepository,
                           OtpService otpService,
                           AuditLogService auditLogService,
                           PasswordEncoder passwordEncoder,
                           @Qualifier("pinEncoder") PasswordEncoder pinEncoder) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.otpService = otpService;
        this.auditLogService = auditLogService;
        this.passwordEncoder = passwordEncoder;
        this.pinEncoder = pinEncoder;
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail()))
            throw BusinessException.conflict("Email already registered");
        if (userRepository.existsByPhoneAndDeletedFalse(request.getPhone()))
            throw BusinessException.conflict("Phone already registered");
        if (userRepository.existsByNationalIdAndDeletedFalse(request.getNationalId()))
            throw BusinessException.conflict("National ID already registered");
        if (Period.between(request.getDateOfBirth(), LocalDate.now()).getYears() < 18)
            throw BusinessException.badRequest("Must be at least 18 years old");

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setNationalId(request.getNationalId());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAddress(request.getAddress());
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setKycStatus(KycStatus.PENDING);
        userRepository.save(user);

        UserCredential credential = new UserCredential();
        credential.setUser(user);
        credential.setPasswordHash(passwordEncoder.encode(request.getPassword())); // Argon2id
        credential.setPinHash(pinEncoder.encode(request.getPin()));                // Argon2id stronger
        credentialRepository.save(credential);

        otpService.sendOtp(user, "EMAIL_VERIFICATION");
        auditLogService.logAsync(user, "REGISTER", "USER", user.getId(), true, null, null);

        return UserResponse.from(user);
    }

    @Override
    public void verifyEmail(VerifyOtpRequest request) {
        User user = userRepository.findByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> BusinessException.notFound("User"));

        if (user.getStatus() != UserStatus.PENDING_VERIFICATION)
            throw BusinessException.badRequest("Account already verified");

        otpService.verifyOtp(user, "EMAIL_VERIFICATION", request.getCode());

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        auditLogService.logAsync(user, "EMAIL_VERIFIED", "USER", user.getId(), true, null, null);
    }

    @Override
    public void resendOtp(String email) {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> BusinessException.notFound("User"));
        if (user.getStatus() != UserStatus.PENDING_VERIFICATION)
            throw BusinessException.badRequest("Account already verified");
        otpService.sendOtp(user, "EMAIL_VERIFICATION");
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> BusinessException.notFound("User"));
    }

    @Override
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("User"));
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        userRepository.save(user);
        auditLogService.logAsync(user, "UPDATE_PROFILE", "USER", user.getId(), true, null, null);
        return UserResponse.from(user);
    }
}
```

---

## 44. module/auth/service/AuthService.java
```java
package com.banking.module.auth.service;

import com.banking.module.auth.dto.request.*;
import java.util.UUID;

public interface AuthService {
    LoginResult login(LoginRequest request, String ipAddress, String userAgent, String deviceFingerprint);
    LoginResult verifyNewDevice(VerifyNewDeviceRequest request, String ipAddress, String userAgent);
    RefreshResult refreshAccessToken(String refreshToken, String ipAddress, String deviceFingerprint);
    void logout(String refreshToken, String accessToken, UUID userId);
}
```

---

## 45. module/auth/service/AuthServiceImpl.java
```java
package com.banking.module.auth.service;

import com.banking.common.enums.UserStatus;
import com.banking.common.exception.*;
import com.banking.infrastructure.redis.*;
import com.banking.module.auth.dto.request.*;
import com.banking.module.notification.service.AuditLogService;
import com.banking.module.user.dto.response.UserResponse;
import com.banking.module.user.entity.*;
import com.banking.module.user.repository.*;
import com.banking.module.user.service.OtpService;
import com.banking.security.JwtTokenProvider;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserCredentialRepository credentialRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDeviceRepository deviceRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditLogService auditLogService;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;
    private final RateLimiterService rateLimiterService;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 30;

    @Override
    public LoginResult login(LoginRequest request, String ipAddress,
                             String userAgent, String deviceFingerprint) {
        rateLimiterService.checkLoginRateLimitByIp(ipAddress);
        rateLimiterService.checkLoginRateLimitByEmail(request.getEmail());

        User user = userRepository.findByEmailWithCredential(request.getEmail())
                .orElseThrow(() -> BusinessException.unauthorized("Invalid credentials"));

        UserCredential credential = user.getCredential();

        if (credential.getLockedUntil() != null && credential.getLockedUntil().isAfter(LocalDateTime.now())) {
            long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), credential.getLockedUntil());
            throw BusinessException.unauthorized("Account locked. Try again in " + minutes + " minutes");
        }

        if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
            handleFailedLogin(credential, user, ipAddress, userAgent);
            throw BusinessException.unauthorized("Invalid credentials"); // Cùng message tránh account enumeration
        }

        if (user.getStatus() == UserStatus.SUSPENDED)
            throw BusinessException.forbidden("Account suspended. Please contact support");
        if (user.getStatus() == UserStatus.PENDING_VERIFICATION)
            throw BusinessException.forbidden("Please verify your email first");

        boolean isNewDevice = handleDevice(user, deviceFingerprint, userAgent, ipAddress);
        if (isNewDevice) {
            otpService.sendOtp(user, "NEW_DEVICE_LOGIN");
            redisService.set("pending:login:" + user.getId(),
                deviceFingerprint + "|" + ipAddress, Duration.ofMinutes(15));
            throw new NewDeviceException(user.getId(), "New device detected. OTP sent to your email");
        }

        credential.setFailedLoginAttempts(0);
        credential.setLockedUntil(null);
        credential.setLastLoginAt(LocalDateTime.now());
        credential.setLastLoginIp(ipAddress);
        credentialRepository.save(credential);

        return issueTokens(user, deviceFingerprint, ipAddress, userAgent);
    }

    @Override
    public LoginResult verifyNewDevice(VerifyNewDeviceRequest request,
                                       String ipAddress, String userAgent) {
        User user = userRepository.findByIdWithCredential(request.getUserId())
                .orElseThrow(() -> BusinessException.notFound("User"));

        otpService.verifyOtp(user, "NEW_DEVICE_LOGIN", request.getOtpCode());

        String pendingKey = "pending:login:" + user.getId();
        String pendingValue = redisService.get(pendingKey)
                .orElseThrow(() -> BusinessException.unauthorized("Session expired. Please login again"));

        String deviceFingerprint = pendingValue.split("\\|")[0];
        redisService.delete(pendingKey);

        deviceRepository.findByUserAndDeviceFingerprint(user, deviceFingerprint)
                .ifPresent(d -> {
                    d.setTrusted(true);
                    d.setTrustedAt(LocalDateTime.now());
                    deviceRepository.save(d);
                });

        return issueTokens(user, deviceFingerprint, ipAddress, userAgent);
    }

    @Override
    public RefreshResult refreshAccessToken(String refreshToken, String ipAddress, String deviceFingerprint) {
        JWTClaimsSet claims = jwtTokenProvider.parseToken(refreshToken);

        if (!"REFRESH".equals(claims.getClaim("tokenType")))
            throw BusinessException.unauthorized("Invalid token type");

        String tokenDevice = (String) claims.getClaim("deviceFingerprint");
        if (deviceFingerprint != null && !deviceFingerprint.equals(tokenDevice)) {
            log.warn("Device fingerprint mismatch. userId={}", claims.getSubject());
            throw BusinessException.unauthorized("Device mismatch detected");
        }

        RefreshToken stored = refreshTokenRepository.findByTokenHash(DigestUtils.sha256Hex(refreshToken))
                .orElseThrow(() -> BusinessException.unauthorized("Refresh token not found"));

        if (stored.isRevoked()) {
            // Reuse attack → revoke toàn bộ family
            refreshTokenRepository.revokeByFamilyId(stored.getFamilyId(), LocalDateTime.now());
            auditLogService.logAsync(stored.getUser(), "REFRESH_TOKEN_REUSE",
                    "USER", stored.getUser().getId(), false, ipAddress, null);
            throw BusinessException.unauthorized("Security violation. Please login again");
        }

        if (stored.getExpiresAt().isBefore(LocalDateTime.now()))
            throw BusinessException.unauthorized("Refresh token expired");

        stored.setRevoked(true);
        stored.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(stored);

        User user = stored.getUser();
        String sessionId = stored.getSessionId();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user, tokenDevice, sessionId);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user, tokenDevice, sessionId);
        saveRefreshToken(user, newRefreshToken, ipAddress, null,
                stored.getFamilyId(), stored.getGeneration() + 1, sessionId);

        return new RefreshResult(newAccessToken, newRefreshToken);
    }

    @Override
    public void logout(String refreshToken, String accessToken, UUID userId) {
        if (refreshToken != null) {
            refreshTokenRepository.findByTokenHash(DigestUtils.sha256Hex(refreshToken))
                    .ifPresent(t -> refreshTokenRepository.revokeByFamilyId(
                            t.getFamilyId(), LocalDateTime.now()));
        }
        if (accessToken != null) {
            String jti = jwtTokenProvider.extractJti(accessToken);
            Duration ttl = jwtTokenProvider.getRemainingTtl(accessToken);
            if (jti != null && !ttl.isZero())
                redisService.blacklistToken(jti, ttl);
        }
    }

    private LoginResult issueTokens(User user, String deviceFingerprint,
                                    String ipAddress, String userAgent) {
        String sessionId = UUID.randomUUID().toString();
        String familyId = UUID.randomUUID().toString();

        String accessToken = jwtTokenProvider.generateAccessToken(user, deviceFingerprint, sessionId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user, deviceFingerprint, sessionId);
        saveRefreshToken(user, refreshToken, ipAddress, userAgent, familyId, 1, sessionId);

        if (deviceFingerprint != null) {
            deviceRepository.findByUserAndDeviceFingerprint(user, deviceFingerprint)
                    .ifPresent(d -> {
                        d.setLastSeenAt(LocalDateTime.now());
                        d.setLastIpAddress(ipAddress);
                        d.setLoginCount(d.getLoginCount() + 1);
                        deviceRepository.save(d);
                    });
        }

        auditLogService.logAsync(user, "LOGIN", "USER", user.getId(), true, ipAddress, userAgent);
        return new LoginResult(accessToken, refreshToken, UserResponse.from(user));
    }

    private boolean handleDevice(User user, String fingerprint, String userAgent, String ip) {
        if (fingerprint == null) return false;
        return deviceRepository.findByUserAndDeviceFingerprint(user, fingerprint)
                .map(d -> false)
                .orElseGet(() -> {
                    UserDevice device = new UserDevice();
                    device.setUser(user);
                    device.setDeviceFingerprint(fingerprint);
                    device.setDeviceName(parseDeviceName(userAgent));
                    device.setDeviceType(parseDeviceType(userAgent));
                    device.setTrusted(false);
                    device.setLastIpAddress(ip);
                    device.setLastSeenAt(LocalDateTime.now());
                    deviceRepository.save(device);
                    return true;
                });
    }

    private void handleFailedLogin(UserCredential credential, User user, String ip, String userAgent) {
        int attempts = credential.getFailedLoginAttempts() + 1;
        credential.setFailedLoginAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            credential.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
            auditLogService.logAsync(user, "ACCOUNT_LOCKED", "USER",
                    user.getId(), false, ip, userAgent);
        }
        credentialRepository.save(credential);
    }

    private void saveRefreshToken(User user, String token, String ip, String userAgent,
                                  String familyId, int generation, String sessionId) {
        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(DigestUtils.sha256Hex(token));
        entity.setExpiresAt(LocalDateTime.now().plusDays(7));
        entity.setIpAddress(ip);
        entity.setUserAgent(userAgent);
        entity.setFamilyId(familyId);
        entity.setGeneration(generation);
        entity.setSessionId(sessionId);
        refreshTokenRepository.save(entity);
    }

    private String parseDeviceName(String ua) {
        if (ua == null) return "Unknown";
        if (ua.contains("iPhone")) return "Safari on iPhone";
        if (ua.contains("Android")) return "Chrome on Android";
        if (ua.contains("Windows")) return "Chrome on Windows";
        if (ua.contains("Macintosh")) return "Safari on Mac";
        return "Unknown Device";
    }

    private String parseDeviceType(String ua) {
        if (ua == null) return "UNKNOWN";
        if (ua.contains("Mobile") || ua.contains("iPhone") || ua.contains("Android")) return "MOBILE";
        if (ua.contains("iPad") || ua.contains("Tablet")) return "TABLET";
        return "DESKTOP";
    }
}
```

---

## 46. module/auth/util/CookieUtil.java
```java
package com.banking.module.auth.util;

import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.*;

@Component
public class CookieUtil {

    @Value("${app.cookie.domain:localhost}")
    private String domain;

    @Value("${app.cookie.secure:false}")
    private boolean secure;

    public ResponseCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from("access_token", token)
                .httpOnly(true).secure(secure).path("/")
                .maxAge(Duration.ofMinutes(15))
                .sameSite("Strict").domain(domain).build();
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from("refresh_token", token)
                .httpOnly(true).secure(secure)
                .path("/api/v1/auth/refresh")    // chỉ gửi đến endpoint refresh
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict").domain(domain).build();
    }

    public ResponseCookie deleteAccessTokenCookie() {
        return ResponseCookie.from("access_token", "")
                .httpOnly(true).secure(secure).path("/")
                .maxAge(Duration.ZERO).sameSite("Strict").domain(domain).build();
    }

    public ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true).secure(secure)
                .path("/api/v1/auth/refresh").maxAge(Duration.ZERO)
                .sameSite("Strict").domain(domain).build();
    }

    public Optional<String> extractFromCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
```

---

## 47. module/auth/controller/AuthController.java
```java
package com.banking.module.auth.controller;

import com.banking.common.exception.BusinessException;
import com.banking.common.response.ApiResponse;
import com.banking.module.auth.dto.request.*;
import com.banking.module.auth.service.*;
import com.banking.module.auth.util.CookieUtil;
import com.banking.module.user.dto.request.*;
import com.banking.module.user.dto.response.UserResponse;
import com.banking.module.user.service.UserService;
import com.banking.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final CookieUtil cookieUtil;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful. Please verify your email.",
                        userService.register(request)));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        userService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully.", null));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@RequestParam @Email String email) {
        userService.resendOtp(email);
        return ResponseEntity.ok(ApiResponse.success("OTP sent.", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {

        LoginResult result = authService.login(request, getClientIp(httpRequest),
                httpRequest.getHeader("User-Agent"), request.getDeviceFingerprint());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(result.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCookie(result.refreshToken()).toString())
                .body(ApiResponse.success(result.userResponse()));
    }

    @PostMapping("/verify-new-device")
    public ResponseEntity<ApiResponse<UserResponse>> verifyNewDevice(
            @Valid @RequestBody VerifyNewDeviceRequest request, HttpServletRequest httpRequest) {

        LoginResult result = authService.verifyNewDevice(request,
                getClientIp(httpRequest), httpRequest.getHeader("User-Agent"));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(result.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCookie(result.refreshToken()).toString())
                .body(ApiResponse.success(result.userResponse()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(HttpServletRequest httpRequest) {
        String refreshToken = cookieUtil.extractFromCookie(httpRequest, "refresh_token")
                .orElseThrow(() -> BusinessException.unauthorized("Refresh token not found"));
        String deviceFingerprint = httpRequest.getHeader("X-Device-Fingerprint");

        RefreshResult result = authService.refreshAccessToken(
                refreshToken, getClientIp(httpRequest), deviceFingerprint);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(result.newAccessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCookie(result.newRefreshToken()).toString())
                .body(ApiResponse.success(null));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpRequest,
            @AuthenticationPrincipal UserPrincipal principal) {

        authService.logout(
                cookieUtil.extractFromCookie(httpRequest, "refresh_token").orElse(null),
                cookieUtil.extractFromCookie(httpRequest, "access_token").orElse(null),
                principal.getId());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieUtil.deleteAccessTokenCookie().toString())
                .header(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshTokenCookie().toString())
                .body(ApiResponse.success("Logged out successfully.", null));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(userService.getById(principal.getId())));
    }

    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.updateProfile(principal.getId(), request)));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank())
            return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
```

---

## 48. config/SecurityConfig.java
```java
package com.banking.config;

import com.banking.security.CustomJwtAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.*;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.*;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomJwtAuthenticationConverter jwtAuthenticationConverter;

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/register", "/api/v1/auth/login",
            "/api/v1/auth/refresh",  "/api/v1/auth/verify-otp",
            "/api/v1/auth/resend-otp", "/api/v1/auth/verify-new-device",
            "/v3/api-docs/**", "/swagger-ui/**", "/actuator/health"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                .bearerTokenResolver(cookieBearerTokenResolver())
                .authenticationEntryPoint(authEntryPoint()))
            .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler()));
        return http.build();
    }

    @Bean
    public BearerTokenResolver cookieBearerTokenResolver() {
        return request -> {
            if (request.getCookies() != null)
                for (var c : request.getCookies())
                    if ("access_token".equals(c.getName())) return c.getValue();
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (header != null && header.startsWith("Bearer ")) return header.substring(7);
            return null;
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Argon2id: saltLength=16, hashLength=32, parallelism=1, memory=64MB, iterations=3
        return new Argon2PasswordEncoder(16, 32, 1, 65536, 3);
    }

    @Bean("pinEncoder")
    public PasswordEncoder pinEncoder() {
        // PIN chỉ có 1 triệu giá trị → tăng iterations và memory
        return new Argon2PasswordEncoder(16, 32, 1, 131072, 5);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "https://yourdomain.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private AuthenticationEntryPoint authEntryPoint() {
        return (req, res, ex) -> {
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setStatus(401);
            res.getWriter().write("{\"success\":false,\"code\":401,\"errorKey\":\"UNAUTHORIZED\",\"message\":\"Unauthorized\"}");
        };
    }

    private AccessDeniedHandler accessDeniedHandler() {
        return (req, res, ex) -> {
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setStatus(403);
            res.getWriter().write("{\"success\":false,\"code\":403,\"errorKey\":\"FORBIDDEN\",\"message\":\"Access denied\"}");
        };
    }
}
```

---

## 49. config/AuditConfig.java
```java
package com.banking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "securityAuditorAware")
public class AuditConfig {}
```

---

## 50. config/AsyncConfig.java
```java
package com.banking.config;

import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("auditExecutor")
    public Executor auditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("audit-");
        executor.initialize();
        return executor;
    }
}
```

---

## 51. scheduler/RefreshTokenCleanupScheduler.java
```java
package com.banking.scheduler;

import com.banking.module.user.repository.RefreshTokenRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 3 * * *") // 3:00 AM hàng ngày
    @Transactional
    public void cleanup() {
        refreshTokenRepository.deleteExpiredAndRevoked(LocalDateTime.now());
        log.info("Cleaned up expired and revoked refresh tokens");
    }
}
```

---

## 52. application.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/banking_db
    username: postgres
    password: postgres
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          time_zone: UTC
    open-in-view: false

  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms

  security:
    oauth2:
      resourceserver:
        jwt:
          public-key-location: classpath:certs/public.pem

  flyway:
    enabled: true
    locations: classpath:db/migration

app:
  jwt:
    private-key-location: classpath:certs/private.pem
    access-token-expiry: 900        # 15 phút
    refresh-token-expiry: 604800    # 7 ngày
    issuer: banking-app
  cookie:
    domain: localhost
    secure: false                   # true khi production HTTPS
```

---

## 53. db/migration/V1__init_auth.sql
```sql
-- USERS
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    phone           VARCHAR(15)  NOT NULL UNIQUE,
    national_id     VARCHAR(20)  UNIQUE,
    date_of_birth   DATE         NOT NULL,
    address         VARCHAR(255),
    avatar_url      VARCHAR(255),
    status          VARCHAR(30)  NOT NULL DEFAULT 'PENDING_VERIFICATION',
    kyc_status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    kyc_verified_at TIMESTAMP,
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    deleted_by      VARCHAR(50),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    version         BIGINT       NOT NULL DEFAULT 0
);

-- USER CREDENTIALS
CREATE TABLE user_credentials (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id               UUID NOT NULL UNIQUE REFERENCES users(id),
    password_hash         TEXT NOT NULL,
    pin_hash              TEXT NOT NULL,
    failed_login_attempts INT  NOT NULL DEFAULT 0,
    failed_pin_attempts   INT  NOT NULL DEFAULT 0,
    locked_until          TIMESTAMP,
    two_factor_enabled    BOOLEAN  NOT NULL DEFAULT FALSE,
    two_factor_secret     TEXT,
    last_login_at         TIMESTAMP,
    last_login_ip         VARCHAR(45),
    created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP,
    created_by            VARCHAR(50),
    updated_by            VARCHAR(50),
    version               BIGINT NOT NULL DEFAULT 0
);

-- REFRESH TOKENS
CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id),
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    expires_at  TIMESTAMP   NOT NULL,
    revoked     BOOLEAN     NOT NULL DEFAULT FALSE,
    revoked_at  TIMESTAMP,
    family_id   VARCHAR(36) NOT NULL,
    generation  INT         NOT NULL DEFAULT 1,
    session_id  VARCHAR(36) NOT NULL,
    ip_address  VARCHAR(45),
    user_agent  VARCHAR(500),
    device_id   VARCHAR(50),
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    created_by  VARCHAR(50),
    updated_by  VARCHAR(50),
    version     BIGINT      NOT NULL DEFAULT 0
);
CREATE INDEX idx_refresh_family ON refresh_tokens(family_id);

-- OTPS
CREATE TABLE otps (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id),
    code_hash   VARCHAR(64) NOT NULL,
    type        VARCHAR(30) NOT NULL,
    expires_at  TIMESTAMP   NOT NULL,
    used        BOOLEAN     NOT NULL DEFAULT FALSE,
    used_at     TIMESTAMP,
    ip_address  VARCHAR(45),
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    created_by  VARCHAR(50),
    updated_by  VARCHAR(50),
    version     BIGINT      NOT NULL DEFAULT 0
);
CREATE INDEX idx_otp_user_type ON otps(user_id, type, used);

-- USER DEVICES
CREATE TABLE user_devices (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID         NOT NULL REFERENCES users(id),
    device_fingerprint  VARCHAR(64)  NOT NULL,
    device_name         VARCHAR(100) NOT NULL,
    device_type         VARCHAR(20),
    trusted             BOOLEAN      NOT NULL DEFAULT FALSE,
    trusted_at          TIMESTAMP,
    last_ip_address     VARCHAR(45),
    last_seen_at        TIMESTAMP,
    login_count         INT          NOT NULL DEFAULT 0,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    created_by          VARCHAR(50),
    updated_by          VARCHAR(50),
    version             BIGINT       NOT NULL DEFAULT 0
);
CREATE INDEX idx_device_user        ON user_devices(user_id);
CREATE INDEX idx_device_fingerprint ON user_devices(device_fingerprint);

-- AUDIT LOGS
CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES users(id),
    action          VARCHAR(50)  NOT NULL,
    entity_type     VARCHAR(50)  NOT NULL,
    entity_id       UUID,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(500),
    success         BOOLEAN      NOT NULL,
    failure_reason  VARCHAR(500),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_audit_user    ON audit_logs(user_id);
CREATE INDEX idx_audit_action  ON audit_logs(action);
CREATE INDEX idx_audit_created ON audit_logs(created_at);
```

---

## Lệnh generate RSA key pair
```bash
mkdir -p src/main/resources/certs
openssl genrsa -out src/main/resources/certs/private.pem 2048
openssl rsa -in src/main/resources/certs/private.pem -pubout -out src/main/resources/certs/public.pem
```

---

## Tổng số file: 53 files
| Layer | Files |
|---|---|
| Config | 3 |
| Common | 5 |
| Security | 5 |
| Infrastructure | 3 |
| Entities | 5 |
| Repositories | 6 |
| DTOs | 8 |
| Services | 8 |
| Controller | 1 |
| Scheduler | 1 |
| Resources | 3 |
| **Total** | **53** |
