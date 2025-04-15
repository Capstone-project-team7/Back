package com.capstone.meerkatai.user.dto;

import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * 로그아웃 요청 정보를 담는 DTO 클래스
 */
@Getter
@Setter
public class LogoutRequest {
  /**
   * 로그아웃할 사용자의 ID
   * <p>
   * null이 허용되지 않는 필수 입력 항목입니다.
   * 이 ID를 통해 로그아웃 처리할 사용자를 식별합니다.
   * </p>
   */
  @NotNull
  @JsonProperty("user_id")
  private Integer userId;
}