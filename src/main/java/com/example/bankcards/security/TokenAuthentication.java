package com.example.bankcards.security;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Getter
public class TokenAuthentication extends UsernamePasswordAuthenticationToken {

    private final TokenData tokenData;

    public TokenAuthentication(TokenData tokenData) {
        super(tokenData.getUsername(), null, tokenData.getAuthorities());
        this.tokenData = tokenData;
    }

}
