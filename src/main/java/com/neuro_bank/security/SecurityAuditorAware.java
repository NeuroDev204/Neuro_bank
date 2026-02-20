package com.neuro_bank.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityAuditorAware implements AuditorAware<String> {
  @Override
  public Optional<String> getCurrentAuditor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if(authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")){
      return Optional.of("SYSTEM");
    }
    if(authentication.getPrincipal() instanceof  UserPrincipal principal){
      return Optional.of(principal.getId().toString());
    }
    return Optional.of("SYSTEM");
  }

}
