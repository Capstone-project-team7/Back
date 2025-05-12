package com.capstone.meerkatai.cctv.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CCTV 추가 요청을 위한 DTO(Data Transfer Object) 클래스입니다.
 * <p>
 * 이 클래스는 클라이언트에서 새로운 CCTV를 추가할 때 필요한 데이터를 담습니다.
 * 모든 필드는 snake_case 형식으로 JSON 직렬화/역직렬화됩니다.
 * </p>
 * <p>
 * 예시 JSON:
 * </p>
 * <pre>
 * {
 *   "cctv_name": "거실 CCTV",
 *   "ip_address": "192.168.1.100",
 *   "cctv_admin": "admin",
 *   "cctv_path": "/video/stream",
 *   "cctv_password": "secure_password",
 *   "user_id": 1
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CctvAddRequest {
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
   * CCTV 접속에 필요한 비밀번호입니다.
   * JSON 필드명: cctv_password
   */
  @JsonProperty("cctv_password")
  private String cctvPassword;

  /**
   * CCTV를 소유한 사용자의 ID입니다.
   * JSON 필드명: user_id
   */
  @JsonProperty("user_id")
  private Long userId;
}