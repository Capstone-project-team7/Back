package com.capstone.meerkatai.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * 사용자 정보 조회 응답 정보를 담는 DTO 클래스입니다.
 */
@Getter
@Builder
public class UserInfoResponse {

  @JsonProperty("user_id")
  private Integer userId;

  @JsonProperty("user_email")
  private String userEmail;

  @JsonProperty("user_name")
  private String userName;

  @JsonProperty("notify_status")
  private Boolean notifyStatus;

  @JsonProperty("agreement_status")
  private Boolean agreementStatus;

  @JsonProperty("first_login")
  private Boolean firstLogin;
}