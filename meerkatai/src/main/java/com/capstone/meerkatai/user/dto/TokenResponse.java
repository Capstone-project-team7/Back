package com.capstone.meerkatai.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JWT 토큰 정보를 담는 응답 DTO 클래스입니다.
 * <p>
 * 이 클래스는 인증 성공 후 클라이언트에게 반환되는 JWT 토큰 정보를 담습니다.
 * Access Token과 Refresh Token을 포함하여, OAuth2 표준 형식을 따릅니다.
 * </p>
 * @see com.capstone.meerkatai.global.jwt.JwtUtil
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

  private String accessToken;

  /**
   * 리프레시 토큰입니다.
   * <p>
   * 액세스 토큰이 만료되었을 때 새로운 액세스 토큰을 발급받기 위해 사용됩니다.
   * 비교적 긴 만료 시간을 가집니다.
   * </p>
   */
  private String refreshToken;

  /**
   * 토큰의 타입입니다.
   * <p>
   * 일반적으로 "Bearer" 값을 가집니다.
   * 클라이언트는 이 타입을 Authorization 헤더의 접두어로 사용해야 합니다.
   * </p>
   */
  private String grantType;

  /**
   * 액세스 토큰의 만료 시간입니다. (초 단위)
   * <p>
   * 클라이언트는 이 시간 내에 토큰을 갱신해야 합니다.
   * </p>
   */
  private Long expiresIn;
}