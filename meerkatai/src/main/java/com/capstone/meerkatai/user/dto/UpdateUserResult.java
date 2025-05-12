package com.capstone.meerkatai.user.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 정보 수정 결과를 담는 클래스입니다.
 * 응답 데이터와 메시지를 함께 포함합니다.
 */
@Getter
@RequiredArgsConstructor
public class UpdateUserResult {
    /**
     * 사용자 정보 수정 응답 데이터
     */
    private final UpdateUserResponse response;
    
    /**
     * 사용자 정보 수정 결과 메시지
     */
    private final String message;
} 