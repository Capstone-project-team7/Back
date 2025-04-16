package com.capstone.meerkatai.common.exception;

/**
 * 요청한 리소스를 찾을 수 없을 때 발생하는 예외 클래스입니다.
 * <p>
 * 이 예외는 데이터베이스나 다른 저장소에서 특정 ID, 이름 또는 다른 식별자로
 * 리소스를 검색했을 때 해당 리소스가 존재하지 않는 경우에 사용됩니다.
 * 주로 CCTV, User와 같은 엔티티를 조회할 때 해당 ID의 엔티티가 없는 경우 발생합니다.
 * </p>
 * <p>
 * 이 예외는 {@link GlobalExceptionHandler}에 의해 처리되어
 * 클라이언트에게 HTTP 404(Not Found) 응답을 반환합니다.
 * </p>
 */
public class ResourceNotFoundException extends RuntimeException {

  /**
   * 사용자 정의 메시지로 예외를 생성합니다.
   *
   * @param message 예외에 대한 상세 메시지
   */
  public ResourceNotFoundException(String message) {
    super(message);
  }

  /**
   * 리소스 종류, 필드명, 필드값을 기반으로 포맷팅된 메시지와 함께 예외를 생성합니다.
   * 메시지 형식: "{resource} not found with {field}: {value}"
   *
   * @param resource 찾을 수 없는 리소스의 종류(예: "User", "CCTV")
   * @param field 검색에 사용된 필드명(예: "id", "email")
   * @param value 검색에 사용된 필드값(예: 1, "user@example.com")
   */
  public ResourceNotFoundException(String resource, String field, Object value) {
    super(String.format("%s not found with %s: %s", resource, field, value));
  }
}