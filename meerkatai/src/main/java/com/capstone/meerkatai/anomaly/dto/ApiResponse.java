package com.capstone.meerkatai.anomaly.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * API 응답에 사용되는 표준 포맷 클래스
 * @param <T> 응답 데이터 타입
 */
@Getter
@NoArgsConstructor
public class ApiResponse<T> {
    
    private String status;
    private T data;
    private String message;
    
    private ApiResponse(String status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }
    
    /**
     * 성공 응답 생성
     * @param data 응답 데이터
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data, null);
    }
    
    /**
     * 성공 메시지 응답 생성
     * @param message 성공 메시지
     * @return 성공 응답 객체
     */
    public static ApiResponse<String> success(String message) {
        return new ApiResponse<>("success", null, message);
    }
    
    /**
     * 성공 응답 생성 (데이터와 메시지 모두 포함)
     * @param data 응답 데이터
     * @param message 성공 메시지
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>("success", data, message);
    }
    
    /**
     * 에러 응답 생성
     * @param message 에러 메시지
     * @return 에러 응답 객체
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", null, message);
    }
} 