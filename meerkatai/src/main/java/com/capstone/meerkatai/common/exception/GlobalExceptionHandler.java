package com.capstone.meerkatai.common.exception;

import com.capstone.meerkatai.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 애플리케이션에서 발생하는 모든 예외를 중앙에서 처리하는 글로벌 예외 핸들러입니다.
 * <p>
 * 이 클래스는 애플리케이션 전체에서 발생하는 다양한 종류의 예외를 캡처하고,
 * 각 예외 타입에 맞는 적절한 HTTP 상태 코드와 응답 메시지를 클라이언트에게 반환합니다.
 * 모든 예외는 일관된 형식의 API 응답으로 변환되어 클라이언트에게 전달됩니다.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * 리소스를 찾을 수 없을 때 발생하는 ResourceNotFoundException을 처리합니다.
   *
   * @param ex 처리할 ResourceNotFoundException 객체
   * @return HTTP 404(Not Found) 상태 코드와 오류 메시지가 포함된 API 응답
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
    log.error("Resource not found: {}", ex.getMessage());
    ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  /**
   * 비즈니스 로직 실행 중 발생하는 BusinessException을 처리합니다.
   *
   * @param ex 처리할 BusinessException 객체
   * @return HTTP 400(Bad Request) 상태 코드와 오류 메시지가 포함된 API 응답
   */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
    log.error("Business exception: {}", ex.getMessage());
    ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  /**
   * 다른 모든 예외를 처리하는 기본 핸들러입니다.
   * 예상치 못한 서버 오류나 처리되지 않은 예외를 캡처합니다.
   *
   * @param ex 처리할 Exception 객체
   * @return HTTP 500(Internal Server Error) 상태 코드와 일반적인 오류 메시지가 포함된 API 응답
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
    log.error("Unexpected error: ", ex);
    ApiResponse<Void> response = ApiResponse.error("서버 오류가 발생했습니다.");
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}