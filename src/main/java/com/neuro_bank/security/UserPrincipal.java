package com.neuro_bank.security;

import com.neuro_bank.common.enums.UserStatus;
import com.neuro_bank.module.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UserPrincipal implements UserDetails {
  private final UUID id;
  private final String email;
  private final String phone;
  private final String password;
  private final UserStatus status;
  private final Collection<? extends GrantedAuthority> authorities;

  public UserPrincipal(User user){
    this.id = user.getId();
    this.email = user.getEmail();
    this.phone = user.getPhone();
    this.password = user.getCredential().getPasswordHash();
    this.status = user.getStatus();
    this.authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }
  @Override
  public String getUsername(){
    return email;
  }
  public UserStatus getStatus(){return status;}
  public String getPhone(){
    return phone;
  }
  @Override
  public String getPassword(){
    return password;
  }
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities(){
    return authorities;
  }
  @Override
  public boolean isAccountNonLocked(){
    return status != UserStatus.SUSPENDED;
  }
  @Override
  public boolean isEnabled(){
    return status == UserStatus.ACTIVE;
  }
  @Override
  public boolean isAccountNonExpired(){
    return true;
  }
  @Override
  public boolean isCredentialsNonExpired(){
    return true;
  }

  public UUID getId() {
    return id;
  }
}
