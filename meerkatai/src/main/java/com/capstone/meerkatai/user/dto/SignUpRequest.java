package com.capstone.meerkatai.user.dto;

import com.capstone.meerkatai.user.entity.User;
import com.capstone.meerkatai.user.entity.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원가입 요청 정보를 담는 DTO 클래스입니다.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

  @NotBlank(message = "이메일은 필수 입력 항목입니다")
  @Email(message = "올바른 형식의 이메일 주소여야 합니다")
  @JsonProperty("user_email")
  private String userEmail;

  @NotBlank(message = "비밀번호는 필수 입력 항목입니다")
  @JsonProperty("user_password")
  private String userPassword;

  @NotBlank(message = "이름은 필수 입력 항목입니다")
  @JsonProperty("user_name")
  private String userName;

  @NotNull(message = "약관 동의 여부는 필수 입력 항목입니다")
  @JsonProperty("agreement_status")
  private Boolean agreementStatus;

  public User toEntity() {
    return User.builder()
        .email(userEmail)
        .password(userPassword)
        .name(userName)
        .notification(true)
        .agreement(agreementStatus)
        .firstLogin(true)
        .role(Role.USER)
        .build();
  }
}