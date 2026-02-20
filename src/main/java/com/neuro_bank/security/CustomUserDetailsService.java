package com.neuro_bank.security;

import com.neuro_bank.module.user.entity.User;
import com.neuro_bank.module.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomUserDetailsService implements UserDetailsService {

  UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmailWithCredential(email)
        .orElseThrow(()->new UsernameNotFoundException("User not found: "+email));
    return new UserPrincipal(user);
  }
  //load bang uuid tu jwt subject
  public UserDetails loadByUserId(UUID id){
    User user = userRepository.findByIdWithCredential(id)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: "+ id));
    return new UserPrincipal(user);
  }
}
