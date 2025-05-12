package com.capstone.meerkatai.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 로그인 요청 정보를 담는 DTO 클래스입니다.
 */
@Getter
@Setter
public class SignInRequest {

  @NotBlank(message = "이메일은 필수 입력 항목입니다")
  @Email(message = "올바른 형식의 이메일 주소여야 합니다")
  @JsonProperty("user_email")
  private String userEmail;

  @NotBlank(message = "비밀번호는 필수 입력 항목입니다")
  @JsonProperty("user_password")
  private String userPassword;
}