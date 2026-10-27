package com.backend.core.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static com.backend.utility.DataTypeUtility.*;

/**
 * JwtTokenService - Login pe signed JWT banata hai aur har protected request pe verify karta hai.
 * Secret application.properties ke jwt.secret (env JWT_SECRET) se aata hai, token ke andar nahi jaata.
 */
@Service
public class JwtTokenService {

    public static final String CLAIM_GYM_CODE = "gym_code";
    public static final String CLAIM_USER_ID = "user_id";
    public static final String CLAIM_ROLE = "role";

    private final SecretKey key;
    private final long expiryMillis;

    public JwtTokenService(@Value("${jwt.secret}") String secret,
                           @Value("${jwt.expiry-hours:168}") long expiryHours) {
        // HS256 ke liye kam se kam 32 bytes ka secret chahiye, warna startup pe hi error
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMillis = expiryHours * 60L * 60L * 1000L;
    }

    public String generateToken(String gymCode, String username, Long userId, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_GYM_CODE, gymCode)
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_ROLE, role)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiryMillis))
                .signWith(key)
                .compact();
    }

    /**
     * Valid token ho to claims, warna (galat signature / expire / khaali) null.
     */
    public Claims parseToken(String token) {
        if (stringValue(token).isEmpty()) return null;
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}
