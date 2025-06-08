package com.capstone.meerkatai.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 회원 탈퇴 요청 정보를 담는 DTO 클래스입니다.
 */
@Getter
@Setter
public class WithdrawRequest {
//  /**
//   * 탈퇴할 사용자의 ID입니다.
//   * <p>
//   * null이 허용되지 않는 필수 입력 항목입니다.
//   * 이 ID를 통해 탈퇴 처리할 사용자를 식별합니다.
//   * </p>
//   */
//  @NotNull(message = "사용자 ID는 필수 입력 항목입니다")
//  @JsonProperty("user_id")
//  private Long userId;

  /**
   * 사용자의 비밀번호입니다.
   * <p>
   * 빈 값이 허용되지 않는 필수 입력 항목입니다.
   * 본인 확인을 위해 비밀번호를 검증합니다.
   * </p>
   */
  @NotBlank(message = "비밀번호는 필수 입력 항목입니다")
  @JsonProperty("user_password")
  private String userPassword;
}