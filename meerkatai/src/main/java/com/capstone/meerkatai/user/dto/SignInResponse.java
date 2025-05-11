package com.capstone.meerkatai.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * 로그인 요청에 대한 응답 DTO입니다.
 */
@Getter
@Builder
public class SignInResponse {

  /**
   * JWT 인증 토큰
   * 클라이언트는 이 토큰을 Authorization 헤더에 포함하여 보호된 리소스에 접근
   */
  private String token;

  /**
   * 토큰의 만료 시간입니다. (초 단위)
   */
  private Integer expiresIn;

  /**
   * 인증된 사용자의 고유 식별자입니다.
   */
  @JsonProperty("user_id")
  private Long userId;

  /**
   * 사용자의 이름입니다.
   */
  @JsonProperty("user_name")
  private String userName;

  /**
   * 사용자의 최초 로그인 여부입니다.
   * true인 경우 사용자가 처음으로 로그인한 것을 의미합니다.
   */
  @JsonProperty("first_login")
  private boolean firstLogin;

  /**
   * 사용자에게 할당된 총 저장 공간입니다. (바이트 단위)
   */
  @JsonProperty("total_space")
  private Long totalSpace;

  /**
   * 사용자가 현재 사용 중인 저장 공간입니다. (바이트 단위)
   */
  @JsonProperty("used_space")
  private Long usedSpace;

  /**
   * 응답 예시:
   * <pre>
   * {
   *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
   *   "expiresIn": 86400,
   *   "user_id": 123,
   *   "user_name": "홍길동",
   *   "first_login": false,
   *   "total_space": 1073741824,
   *   "used_space": 52428800
   * }
   * </pre>
   */
}