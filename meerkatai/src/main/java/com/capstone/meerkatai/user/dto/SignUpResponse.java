package com.capstone.meerkatai.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * 회원가입 요청에 대한 응답 DTO입니다.
 */
@Getter
@Builder
public class SignUpResponse {


  @JsonProperty("user_id")
  private Integer userId;

  @JsonProperty("user_email")
  private String userEmail;

  @JsonProperty("user_name")
  private String userName;

  /**
   * 사용자의 알림 설정 상태입니다.
   * true: 알림 활성화, false: 알림 비활성화
   */
  @JsonProperty("notify_status")
  private Boolean notifyStatus;

  /**
   * 사용자의 서비스 이용 약관 동의 상태입니다.
   * true: 동의, false: 미동의
   */
  @JsonProperty("agreement_status")
  private Boolean agreementStatus;

  /**
   * 응답 예시:
   * <pre>
   * {
   *   "user_id": 123,
   *   "user_email": "user@example.com",
   *   "user_name": "홍길동",
   *   "notify_status": true,
   *   "agreement_status": true
   * }
   * </pre>
   */
}