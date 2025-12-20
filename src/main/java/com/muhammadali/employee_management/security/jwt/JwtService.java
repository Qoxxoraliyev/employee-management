package com.muhammadali.employee_management.security.jwt;

import com.muhammadali.employee_management.exceptions.AuthenticationFailedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Component
public class JwtService {

    private final String secret;


    public JwtService(@Value("${jwt.secret}") String secret){
        this.secret=secret;
    }


    public String generateToken(String email){
        Map<String, Object> claims=new HashMap<>();
        return createToken(claims,email);
    }

    private String createToken(Map<String,Object> claims, String email){
        try {
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(email)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30)) // 30 min
                    .signWith(getSignKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            throw new AuthenticationFailedException("Token generation failed", e);
        }
    }


    private Key getSignKey(){
        byte[] keyBytes=secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public String extractUsername(String token){
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            throw new AuthenticationFailedException("Invalid token: cannot extract username", e);
        }
    }


    public Date extractExpiration(String token){
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (Exception e) {
            throw new AuthenticationFailedException("Invalid token: cannot extract expiration", e);
        }
    }


    public <T> T extractClaim(String token, Function<Claims,T> claimsTFunction){
        final Claims claims=extractAllClaims(token);
        return claimsTFunction.apply(claims);
    }

    private Claims extractAllClaims(String token){
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new AuthenticationFailedException("Invalid or expired token", e);
        }
    }


    private Boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        if (!username.equals(userDetails.getUsername()) || isTokenExpired(token)) {
            throw new AuthenticationFailedException("Token validation failed for user: " + userDetails.getUsername());
        }
        return true;
    }


}
