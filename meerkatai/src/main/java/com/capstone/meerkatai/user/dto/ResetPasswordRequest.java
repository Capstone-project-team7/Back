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
  @NotBlank
  @Email
  @JsonProperty("user_email")
  private String userEmail;

  /**
   * 새로 설정할 비밀번호
   * <p>
   * 빈 값이 허용되지 않는 필수 입력 항목
   * 서비스에서 암호화되어 저장
   * </p>
   */
  @NotBlank
  @JsonProperty("new_password")
  private String newPassword;
}