package com.capstone.meerkatai.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import java.time.ZonedDateTime;

/**
 * 사용자 정보 수정 응답 정보를 담는 DTO 클래스입니다.
 */
@Getter
@Builder
public class UpdateUserResponse {
  /**
   * 수정된 사용자의 ID입니다.
   */
  @JsonProperty("user_id")
  private Long userId;

  /**
   * 수정된 사용자의 이름입니다.
   */
  @JsonProperty("user_name")
  private String userName;

  /**
   * 사용자 정보가 업데이트된 시간입니다.
   * ISO 8601 형식의 타임스탬프로 반환됩니다.
   */
  @JsonProperty("updated_at")
  private ZonedDateTime updatedAt;
}