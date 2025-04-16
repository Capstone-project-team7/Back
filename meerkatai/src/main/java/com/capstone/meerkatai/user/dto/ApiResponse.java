package com.capstone.meerkatai.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * API 응답의 표준 형식을 정의하는 래퍼(wrapper) 클래스
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
  /**
   * 응답 상태입니다.
   * "success" 또는 "error" 값을 가집니다.
   */
  private String status;

  /**
   * 실제 응답 데이터입니다.
   * 제네릭 타입 T를 사용하여 다양한 형태의 데이터를 포함할 수 있습니다.
   */
  private T data;

  /**
   * 추가 메시지입니다.
   * 주로 에러 설명이나 성공 메시지를 포함합니다.
   */
  private String message;

  /**
   * 상태와 데이터만을 포함하는 응답을 생성합니다.
   */
  public ApiResponse(String status, T data) {
    this.status = status;
    this.data = data;
  }

  /**
   * 상태와 메시지만을 포함하는 응답을 생성합니다.
   */
  public ApiResponse(String status, String message) {
    this.status = status;
    this.message = message;
  }

  /**
   * 성공 응답을 생성합니다.
   * @apiNote
   * 응답 예시:
   * <pre>
   * {
   *   "status": "success",
   *   "data": { ... },
   *   "message": null
   * }
   * </pre>
   */
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>("success", data);
  }

  /**
   * 메시지만을 포함하는 성공 응답을 생성합니다.
   * @apiNote
   * 응답 예시:
   * <pre>
   * {
   *   "status": "success",
   *   "data": null,
   *   "message": "작업이 성공적으로 완료되었습니다."
   * }
   * </pre>
   */
  public static ApiResponse<Void> success(String message) {
    return new ApiResponse<>("success", message);
  }

  /**
   * 에러 응답을 생성합니다.
   * @apiNote
   * 응답 예시:
   * <pre>
   * {
   *   "status": "error",
   *   "data": null,
   *   "message": "요청을 처리하는 중 오류가 발생했습니다."
   * }
   * </pre>
   */
  public static <T> ApiResponse<T> error(String message) {
    return new ApiResponse<>("error", null, message);
  }
}