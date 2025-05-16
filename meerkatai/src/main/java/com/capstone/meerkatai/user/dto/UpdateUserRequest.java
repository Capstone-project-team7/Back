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
  @NotNull(message = "사용자 ID는 필수 입력 항목입니다")
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
   * 현재 사용자의 비밀번호입니다.
   * <p>
   * 선택적 입력 항목이지만, 비밀번호를 변경하려면 필수입니다.
   * 본인 확인을 위해 사용됩니다.
   * </p>
   */
  @JsonProperty("user_password")
  private String userPassword;
  
  /**
   * 새로 변경할 비밀번호입니다.
   * <p>
   * 선택적 입력 항목입니다.
   * 값이 제공된 경우 현재 비밀번호 확인 후 새 비밀번호로 변경됩니다.
   * </p>
   */
  @JsonProperty("new_password")
  private String newPassword;
}