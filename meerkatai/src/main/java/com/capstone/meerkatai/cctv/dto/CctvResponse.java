package com.capstone.meerkatai.cctv.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * CCTV 목록 조회 응답을 위한 DTO(Data Transfer Object) 클래스입니다.
 * <p>
 * 이 클래스는 CCTV 목록을 클라이언트에게 제공할 때 사용되며,
 * 상세 정보보다는 간략한 CCTV 정보만을 포함합니다.
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
 *   "created_at": "2023-06-15T10:30:00",
 *   "updated_at": "2023-06-15T10:30:00"
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CctvResponse {
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
}