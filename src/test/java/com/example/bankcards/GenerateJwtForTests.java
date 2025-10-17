package com.example.bankcards;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class GenerateJwtForTests {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.life-time}")
    private Long lifeTime;

    public GenerateJwtForTests() {

    }

    public String generateJwtToken(Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", "test");
        claims.put("roles", Set.of("ROLE_" + role.toUpperCase(Locale.ROOT)));
        claims.put("userId", userId);
        return Jwts.builder()
                .claims(claims)
                .subject("test")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + lifeTime))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

}
