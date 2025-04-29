package com.capstone.meerkatai.cctv.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * CCTV 상세 정보 조회 응답을 위한 DTO(Data Transfer Object) 클래스입니다.
 * <p>
 * 이 클래스는 특정 CCTV의 상세 정보를 클라이언트에게 제공할 때 사용됩니다.
 * 기본 정보 외에도 관리자, 접근 경로, 소유자 ID 등의 추가 정보를 포함합니다.
 * 모든 필드는 snake_case 형식으로 JSON 직렬화됩니다.
 * </p>
 * <p>
 * 예시 JSON:
 * </p>
 * <pre>
 * {
 *   "cctv_id": 1,
 *   "cctv_name": "거실 CCTV",
 *   "ip_address": "192.168.1.100",
 *   "cctv_admin": "admin",
 *   "cctv_path": "/video/stream",
 *   "created_at": "2023-06-15T10:30:00",
 *   "updated_at": "2023-06-15T10:30:00",
 *   "user_id": 1
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CctvDetailResponse {
  /**
   * CCTV의 고유 식별자입니다.
   * JSON 필드명: cctv_id
   */
  @JsonProperty("cctv_id")
  private Long cctvId;

  /**
   * CCTV의 이름입니다.
   * JSON 필드명: cctv_name
   */
  @JsonProperty("cctv_name")
  private String cctvName;

  /**
   * CCTV의 IP 주소입니다.
   * JSON 필드명: ip_address
   */
  @JsonProperty("ip_address")
  private String ipAddress;

  /**
   * CCTV의 관리자 계정입니다.
   * JSON 필드명: cctv_admin
   */
  @JsonProperty("cctv_admin")
  private String cctvAdmin;

  /**
   * CCTV의 접근 경로입니다.
   * JSON 필드명: cctv_path
   */
  @JsonProperty("cctv_path")
  private String cctvPath;

  /**
   * CCTV 정보가 생성된 시간입니다.
   * JSON 필드명: created_at
   */
  @JsonProperty("created_at")
  private LocalDateTime createdAt;

  /**
   * CCTV 정보가 마지막으로 업데이트된 시간입니다.
   * JSON 필드명: updated_at
   */
  @JsonProperty("updated_at")
  private LocalDateTime updatedAt;

  /**
   * CCTV를 소유한 사용자의 ID입니다.
   * JSON 필드명: user_id
   */
  @JsonProperty("user_id")
  private Long userId;
}