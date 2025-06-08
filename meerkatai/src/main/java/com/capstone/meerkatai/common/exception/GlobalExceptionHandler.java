package com.capstone.meerkatai.common.exception;

import com.capstone.meerkatai.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
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
   * 유효성 검사(@Valid) 실패 시 발생하는 예외를 처리합니다.
   * 각 필드별 오류 메시지를 추출하여 깔끔한 응답을 반환합니다.
   *
   * @param ex 처리할 MethodArgumentNotValidException 객체
   * @return HTTP 400(Bad Request) 상태 코드와 유효성 검사 오류 메시지가 포함된 API 응답
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    // 첫 번째 오류 메시지만 추출
    FieldError fieldError = ex.getBindingResult().getFieldError();
    String errorMessage = fieldError != null ? fieldError.getDefaultMessage() : "유효성 검사에 실패했습니다";
    
    // 디버그 로그에 상세 정보 기록
    log.debug("유효성 검사 실패: 필드={}, 값={}, 메시지={}",
        fieldError != null ? fieldError.getField() : "unknown",
        fieldError != null ? fieldError.getRejectedValue() : "unknown",
        errorMessage);
    
    ApiResponse<Void> response = ApiResponse.error(errorMessage);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  /**
   * IllegalArgumentException 예외를 처리합니다.
   * 주로 비즈니스 로직에서 잘못된 인자가 전달될 때 발생합니다.
   *
   * @param ex 처리할 IllegalArgumentException 객체
   * @return HTTP 400(Bad Request) 상태 코드와 오류 메시지가 포함된 API 응답
   */
  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
    log.debug("잘못된 인자: {}", ex.getMessage());
    ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }
  
  /**
   * 인증 실패 예외를 처리합니다.
   *
   * @param ex 처리할 BadCredentialsException 객체
   * @return HTTP 401(Unauthorized) 상태 코드와 오류 메시지가 포함된 API 응답
   */
  @ExceptionHandler(BadCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
    log.debug("인증 실패: {}", ex.getMessage());
    ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }
  
  /**
   * Spring Security의 모든 인증 관련 예외를 처리합니다.
   *
   * @param ex 처리할 AuthenticationException 객체
   * @return HTTP 401(Unauthorized) 상태 코드와 오류 메시지가 포함된 API 응답
   */
  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
    log.debug("인증 오류: {}", ex.getMessage());
    ApiResponse<Void> response = ApiResponse.error("인증에 실패했습니다: " + ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }
  
  /**
   * 인증 관련 RuntimeException을 처리합니다.
   * 로그인 과정에서 발생하는 예외를 처리합니다.
   *
   * @param ex 처리할 RuntimeException 객체
   * @return HTTP 400(Bad Request) 상태 코드와 오류 메시지가 포함된 API 응답
   */
  @ExceptionHandler(RuntimeException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
    // 로그인 처리 중 오류인지 확인
    if (ex.getMessage() != null && ex.getMessage().startsWith("로그인 처리 중 오류")) {
      log.error("로그인 처리 오류: {}", ex.getMessage());
      ApiResponse<Void> response = ApiResponse.error("로그인 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
      return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    // 그 외 RuntimeException은 일반 서버 오류로 처리
    log.error("런타임 오류: ", ex);
    ApiResponse<Void> response = ApiResponse.error("요청을 처리하는 중 오류가 발생했습니다.");
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * 리소스를 찾을 수 없을 때 발생하는 ResourceNotFoundException을 처리합니다.
   *
   * @param ex 처리할 ResourceNotFoundException 객체
   * @return HTTP 404(Not Found) 상태 코드와 오류 메시지가 포함된 API 응답
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
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
  @ResponseStatus(HttpStatus.BAD_REQUEST)
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
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
    log.error("Unexpected error: ", ex);
    ApiResponse<Void> response = ApiResponse.error("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}