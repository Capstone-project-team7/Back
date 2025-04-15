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

  @NotBlank
  @Email
  @JsonProperty("user_email")
  private String userEmail;

  @NotBlank
  @JsonProperty("user_password")
  private String userPassword;
}