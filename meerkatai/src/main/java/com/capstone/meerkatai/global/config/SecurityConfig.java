package com.capstone.meerkatai.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.capstone.meerkatai.global.jwt.JwtFilter;
import com.capstone.meerkatai.global.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

// Spring Security 설정을 담당하는 클래스
/*
 1.JWT 기반 인증 처리
 2.비밀번호 암호화
 3.CORS 설정
 4.보안 필터 체인 구성
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;


//Spring Security의 필터 체인
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)  // JWT를 사용하므로 CSRF 보호 비활성화
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // CORS 설정 적용
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // 세션 미사용 (JWT 사용)
        .authorizeHttpRequests(auth -> auth
            // 인증이 필요한 API 경로 설정
            //모든 cctv 경로, 로그인, 회원가입 이외 user 경로
            .requestMatchers("/api/v1/cctv/**","/api/v1/auth/reset-password",
                "/api/v1/auth/info/**","/api/v1/auth/logout","/api/v1/auth/update",
                "/api/v1/auth/withdraw").authenticated()
            // 인증이 필요없는 API 경로 설정
            .requestMatchers(
                "/api/v1/auth/register",
                "/api/v1/auth/login",
                "/api/v1/auth/refresh",
                "/api/anomaly/notify"
            ).permitAll()
            // 나머지 요청은 모두 허용
            .anyRequest().permitAll()
        )
        .addFilterBefore(new JwtFilter(jwtUtil, userDetailsService), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  //인증 관리자 설정
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

//BCrypt 해시 함수를 사용하여 비밀번호를 안전하게 암호화
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

 //CORS 설정
 @Bean
 public CorsConfigurationSource corsConfigurationSource() {
   CorsConfiguration configuration = new CorsConfiguration();
   configuration.setAllowedOrigins(List.of(
    "http://localhost:5173",  // 기존 프론트엔드 서버 주소
    "http://localhost:8000"   // FastAPI 서버 주소 추가
));
   configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));  // 허용할 HTTP 메서드
   configuration.setAllowedHeaders(List.of("*"));  // 모든 헤더 허용
   configuration.setAllowCredentials(true);  // 인증 정보 포함 허용
   configuration.setMaxAge(3600L);  // 프리플라이트 요청 캐시 시간 (1시간)

   UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
   source.registerCorsConfiguration("/**", configuration);  // 모든 경로에 CORS 설정 적용
   return source;
 }
}
