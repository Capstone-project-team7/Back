package com.capstone.meerkatai.global.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

// JWT 관련 핵심 기능 담당, 생성, 유효성 검증

@Component
public class JwtUtil {

  //JWT 시크릿 키
  @Value("${jwt.secret}")
  private String secretKey;

  // 토큰 만료 시간 (밀리초)
  @Value("${jwt.expiration}")
  private long expirationTime;

  // JWT 서명에 사용할 키
  private Key key;

  //빈 초기화 시 실행되는 메서드, 설정된 시크릿 키를 바이트 배열로 변환하여 JWT 서명용 키를 생성
  @PostConstruct
  public void init() {
    this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
  }

  //사용자 이메일을 기반으로 JWT 토큰을 생성

  public String generateToken(String email) {
    return Jwts.builder()
        .setSubject(email)  // 토큰 제목을 이메일로 설정
        .setIssuedAt(new Date())  // 토큰 발행 시간
        .setExpiration(new Date(System.currentTimeMillis() + expirationTime))  // 토큰 만료 시간
        .signWith(key, SignatureAlgorithm.HS256)  // HS256 알고리즘으로 서명
        .compact();
  }

  //JWT 토큰의 유효성을 검증

  public boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

  //JWT 토큰에서 사용자 이메일을 추출

  public String extractUsername(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  //JWT 토큰이 만료되었는지 확인

  private boolean isTokenExpired(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getExpiration()
        .before(new Date());
  }
}