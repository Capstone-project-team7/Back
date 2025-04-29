package com.capstone.meerkatai.common.exception;

/**
 * 비즈니스 로직 실행 중 발생하는 예외를 처리하기 위한 클래스입니다.
 * <p>
 * 이 예외는 데이터 무결성 위반, 비즈니스 규칙 위반 등과 같은
 * 예상 가능한 비즈니스 오류 상황에서 사용됩니다.
 * </p>
 */
public class BusinessException extends RuntimeException {

  public BusinessException(String message) {
    super(message);
  }

  public BusinessException(String message, Throwable cause) {
    super(message, cause);
  }
}