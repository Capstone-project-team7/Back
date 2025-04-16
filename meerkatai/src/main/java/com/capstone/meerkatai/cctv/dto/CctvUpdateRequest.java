package com.capstone.meerkatai.cctv.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CCTV 정보 수정 요청을 위한 DTO(Data Transfer Object) 클래스입니다.
 * <p>
 * 이 클래스는 클라이언트에서 기존 CCTV 정보를 수정할 때 필요한 데이터를 담습니다.
 * 모든 필드는 snake_case 형식으로 JSON 직렬화/역직렬화됩니다.
 * </p>
 * <p>
 * 예시 JSON:
 * </p>
 * <pre>
 * {
 *   "cctv_name": "업데이트된 CCTV 이름",
 *   "ip_address": "192.168.1.200",
 *   "cctv_path": "/video/stream/new",
 *   "cctv_password": "new_secure_password"  // 선택적 필드, 비어있으면 기존 비밀번호 유지
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CctvUpdateRequest {
  /**
   * 수정할 CCTV의 이름입니다.
   * JSON 필드명: cctv_name
   */
  @JsonProperty("cctv_name")
  private String cctvName;

  /**
   * 수정할 CCTV의 IP 주소입니다.
   * JSON 필드명: ip_address
   */
  @JsonProperty("ip_address")
  private String ipAddress;

  /**
   * 수정할 CCTV의 접근 경로입니다.
   * JSON 필드명: cctv_path
   */
  @JsonProperty("cctv_path")
  private String cctvPath;

  /**
   * 수정할 CCTV의 비밀번호입니다.
   * 비어있거나 null인 경우 기존 비밀번호가 유지됩니다.
   * JSON 필드명: cctv_password
   */
  @JsonProperty("cctv_password")
  private String cctvPassword;
}