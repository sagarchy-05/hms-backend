package com.genc.hms.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.genc.hms.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    // IMPORTANT: Store this key securely (e.g., in application.properties/YML)
    // The key must be base64-encoded and at least 256 bits (32 characters)
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    
    // 24 hours in milliseconds
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration; 

    // --- Public Methods ---

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        // Cast UserDetails to your User entity to access custom fields
        User user = (User) userDetails; 
        
        Map<String, Object> claims = new HashMap<>();
        
        // 1. Storing LOGGED_IN_USER_EMAIL (Subject is already the email/username)
        // 2. Storing ROLE
        claims.put("role", user.getRole().name()); 
        
        // 3. Storing LOGGED_IN_USER_ID
        claims.put("userId", user.getUserId()); 
        
        // 4. Storing LOGGED_IN_ROLE_ID - Assuming role ID is implicit via the role name.
        // If a separate ID is required, fetch it here. For simplicity, we'll rely on the 'role' name.
        
        return buildToken(claims, userDetails, jwtExpiration);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // --- Private/Helper Methods ---
    
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername()) // LOGGED_IN_USER_EMAIL
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
