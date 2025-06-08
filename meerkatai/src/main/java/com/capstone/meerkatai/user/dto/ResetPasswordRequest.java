package com.capstone.meerkatai.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 비밀번호 재설정 요청 정보를 담는 DTO 클래스
 */
@Getter
@Setter
public class ResetPasswordRequest {
  /**
   * 비밀번호를 재설정할 사용자의 이메일 주소
   * <p>
   * 이메일 형식이어야 하며, 필수 입력 항목
   * 이 이메일을 통해 사용자를 식별
   * </p>
   */
  @NotBlank(message = "이메일은 필수 입력 항목입니다")
  @Email(message = "올바른 형식의 이메일 주소여야 합니다")
  @JsonProperty("user_email")
  private String userEmail;

  /**
   * 현재 비밀번호
   * <p>
   * 빈 값이 허용되지 않는 필수 입력 항목
   * 비밀번호 변경 전 본인 확인을 위해 사용
   * </p>
   */
  @NotBlank(message = "현재 비밀번호는 필수 입력 항목입니다")
  @JsonProperty("user_password")
  private String userPassword;

  /**
   * 새로 설정할 비밀번호
   * <p>
   * 빈 값이 허용되지 않는 필수 입력 항목
   * 서비스에서 암호화되어 저장
   * </p>
   */
  @NotBlank(message = "새 비밀번호는 필수 입력 항목입니다")
  @JsonProperty("new_password")
  private String newPassword;
}