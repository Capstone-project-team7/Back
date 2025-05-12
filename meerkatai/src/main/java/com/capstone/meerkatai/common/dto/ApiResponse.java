package com.capstone.meerkatai.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API 응답을 위한 표준 래퍼(wrapper) 클래스입니다.
 * <p>
 * 이 클래스는 모든 REST API 응답에 일관된 형식을 제공하기 위해 사용됩니다.
 * 응답에는 상태(success/error), 데이터, 메시지의 세 가지 주요 요소가 포함됩니다.
 * </p>
 * <p>
 * 예시 성공 응답 JSON:
 * </p>
 * <pre>
 * {
 *   "status": "success",
 *   "data": { ... },
 *   "message": null
 * }
 * </pre>
 * <p>
 * 예시 오류 응답 JSON:
 * </p>
 * <pre>
 * {
 *   "status": "error",
 *   "data": null,
 *   "message": "리소스를 찾을 수 없습니다."
 * }
 * </pre>
 *
 * @param <T> 응답 데이터의 타입
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
  /**
   * 응답 상태입니다. "success" 또는 "error" 값을 가집니다.
   */
  private String status;

  /**
   * 응답 데이터입니다. 성공 시에는 요청한 데이터가 포함되며, 오류 시에는 null이거나 오류 관련 정보가 포함될 수 있습니다.
   */
  private T data;

  /**
   * 응답 메시지입니다. 주로 오류 발생 시 오류 메시지를 전달하는 데 사용됩니다.
   */
  private String message;

  /**
   * 데이터만 포함하는 성공 응답을 생성합니다.
   *
   * @param data 응답에 포함할 데이터
   * @param <T> 데이터의 타입
   * @return 성공 상태와 데이터가 포함된 ApiResponse 객체
   */
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>("success", data, null);
  }

  /**
   * 데이터와 메시지를 포함하는 성공 응답을 생성합니다.
   *
   * @param data 응답에 포함할 데이터
   * @param message 응답에 포함할 메시지
   * @param <T> 데이터의 타입
   * @return 성공 상태, 데이터, 메시지가 포함된 ApiResponse 객체
   */
  public static <T> ApiResponse<T> success(T data, String message) {
    return new ApiResponse<>("success", data, message);
  }

  /**
   * 메시지만 포함하는 오류 응답을 생성합니다.
   *
   * @param message 오류 메시지
   * @param <T> 데이터의 타입
   * @return 오류 상태와 메시지가 포함된 ApiResponse 객체
   */
  public static <T> ApiResponse<T> error(String message) {
    return new ApiResponse<>("error", null, message);
  }

  /**
   * 데이터와 메시지를 포함하는 오류 응답을 생성합니다.
   *
   * @param data 오류 관련 데이터
   * @param message 오류 메시지
   * @param <T> 데이터의 타입
   * @return 오류 상태, 데이터, 메시지가 포함된 ApiResponse 객체
   */
  public static <T> ApiResponse<T> error(T data, String message) {
    return new ApiResponse<>("error", data, message);
  }
}