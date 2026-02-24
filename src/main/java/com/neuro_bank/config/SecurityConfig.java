package com.neuro_bank.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.neuro_bank.security.CustomJwtAuthenticationConverter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final CustomJwtAuthenticationConverter jwtAuthenticationConverter;
  private static final String[] PUBLIC_ENDPOINTS = {
      "/api/v1/auth/register", "/api/v1/auth/login",
      "/api/v1/auth/refresh", "/api/v1/auth/verify-otp",
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
          if ("access_token".equals(c.getName()))
            return c.getValue();
      String header = request.getHeader(HttpHeaders.AUTHORIZATION);
      if (header != null && header.startsWith("Bearer "))
        return header.substring(7);
      return null;
    };
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

  @Bean
  public PasswordEncoder passwordEncoder() {
    // argon2id: saltLength=16, hashLength=32, paralleisim=1, memor=64mb, iterations=3
    return new Argon2PasswordEncoder(16, 32, 1, 65536, 3);
  }

  @Bean("pinEncoder")
  public PasswordEncoder pinEncoder() {
    return new Argon2PasswordEncoder(16, 32, 1, 131072, 5);
  }

  private AuthenticationEntryPoint authEntryPoint() {
    return (req, res, ex) -> {
      res.setContentType(MediaType.APPLICATION_JSON_VALUE);
      res.setStatus(401);
      res.getWriter()
          .write("{\"success\":false,\"code\":401,\"errorKey\":\"UNAUTHORIZED\",\"message\":\"Unauthorized\"}");
    };
  }

  private AccessDeniedHandler accessDeniedHandler() {
    return (req, res, ex) -> {
      res.setContentType(MediaType.APPLICATION_JSON_VALUE);
      res.setStatus(403);
      res.getWriter()
          .write("{\"success\":false,\"code\":403,\"errorKey\":\"FORBIDDEN\",\"message\":\"Access denied\"}");
    };
  }
}