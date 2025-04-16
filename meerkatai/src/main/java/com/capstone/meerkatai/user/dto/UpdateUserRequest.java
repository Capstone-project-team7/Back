package com.capstone.meerkatai.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 사용자 정보 수정 요청 정보를 담는 DTO 클래스입니다.
 */
@Getter
@Setter
public class UpdateUserRequest {
  /**
   * 수정할 사용자의 ID입니다.
   * <p>
   * null이 허용되지 않는 필수 입력 항목입니다.
   * 이 ID를 통해 업데이트할 사용자를 식별합니다.
   * </p>
   */
  @NotNull
  @JsonProperty("user_id")
  private Long userId;

  /**
   * 변경할 사용자의 이름입니다.
   * <p>
   * 선택적 입력 항목입니다.
   * null인 경우 이름이 변경되지 않습니다.
   * </p>
   */
  @JsonProperty("user_name")
  private String userName;

  /**
   * 변경할 사용자의 비밀번호입니다.
   * <p>
   * 선택적 입력 항목입니다.
   * null인 경우 비밀번호가 변경되지 않습니다.
   * 값이 제공된 경우 서비스에서 암호화되어 저장됩니다.
   * </p>
   */
  @JsonProperty("user_password")
  private String userPassword;

  /**
   * 변경할 사용자의 알림 설정 상태입니다.
   * <p>
   * 선택적 입력 항목입니다.
   * null인 경우 알림 설정이 변경되지 않습니다.
   * true: 알림 활성화, false: 알림 비활성화
   * </p>
   */
  @JsonProperty("notify_status")
  private Boolean notifyStatus;
}