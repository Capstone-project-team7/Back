package com.capstone.meerkatai.global.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

// JWT 관련 설정 속성을 관리하는 클래스
// application.properties에서 'jwt' 접두사로 시작하는 설정값들을 자동으로 매핑.

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {


  private String secretKey;
  private long accessTokenValidityInSeconds;
  private long refreshTokenValidityInSeconds;
}