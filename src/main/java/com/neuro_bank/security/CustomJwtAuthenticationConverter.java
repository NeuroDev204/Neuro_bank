package com.neuro_bank.security;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

 CustomUserDetailsService userDetailsService;
  @Nullable
  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    // lay userid tu subject cua jwt
    String subject = jwt.getSubject();
    UUID userId = UUID.fromString(subject);

    // check tokenType -> chi cho ACCESS di qua
    String tokenType = jwt.getClaimAsString("tokenType");
    if(!"ACCESS".equals(tokenType)){
      throw new BadCredentialsException("Invalid token type");
    }
    // check user status tu db
    // vi jwt van valid du user bi suspended sau khi issue
    UserDetails userDetails=  userDetailsService.loadByUserId(userId);
    return new UsernamePasswordAuthenticationToken(
        userDetails,
        jwt,
        userDetails.getAuthorities()
    );
  }
}
