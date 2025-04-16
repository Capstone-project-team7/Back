package com.capstone.meerkatai.global.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//JWT 인증 처리 필터

public class JwtFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;

  public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
  }

  //HTTP 요청에 대한 JWT 인증을 처리
//Authorization 헤더에서 JWT 토큰 추출

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String token = extractToken(request);  // 요청 헤더에서 토큰 추출

    if (StringUtils.hasText(token)) {
      try {
        String email = jwtUtil.extractUsername(token);  // 토큰에서 이메일 추출
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);  // 사용자 정보 로드

        if (jwtUtil.validateToken(token, userDetails)) {  // 토큰 유효성 검증
          // 인증 정보 생성 및 설정
          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      } catch (Exception e) {
        // 토큰이 유효하지 않은 경우 인증 처리하지 않음
      }
    }

    filterChain.doFilter(request, response);  // 다음 필터로 요청 전달
  }


  //Authorization 헤더에서 JWT 토큰을 추출합니다. Bearer 토큰 형식: "Bearer {JWT토큰}"

  private String extractToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);  // "Bearer " 제거
    }
    return null;
  }
}