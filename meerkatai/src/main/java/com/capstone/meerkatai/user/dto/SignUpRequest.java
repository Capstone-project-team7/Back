package com.capstone.meerkatai.user.dto;

import com.capstone.meerkatai.user.entity.User;
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

  @NotBlank
  @Email
  @JsonProperty("user_email")
  private String userEmail;

  @NotBlank
  @JsonProperty("user_password")
  private String userPassword;


  @NotBlank
  @JsonProperty("user_name")
  private String userName;

  @NotNull
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
        .build();
  }
}