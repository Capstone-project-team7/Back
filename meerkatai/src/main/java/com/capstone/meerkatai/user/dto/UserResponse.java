package com.capstone.meerkatai.user.dto;

import java.time.LocalDateTime;

import com.capstone.meerkatai.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 엔티티를 API 응답으로 변환한 DTO 클래스입니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  /**
   * 사용자의 고유 식별자입니다.
   */
  private Integer userId;

  /**
   * 사용자의 이메일 주소입니다.
   * 로그인 시 ID로 사용됩니다.
   */
  private String email;

  /**
   * 사용자의 이름입니다.
   */
  private String name;

  /**
   * 사용자의 알림 설정 상태입니다.
   * true: 알림 활성화, false: 알림 비활성화
   */
  private boolean notification;

  /**
   * 사용자의 서비스 이용 약관 동의 상태입니다.
   * true: 동의, false: 미동의
   */
  private boolean agreement;

  /**
   * 사용자의 최초 로그인 여부입니다.
   * true: 최초 로그인, false: 그 외
   */
  private boolean firstLogin;


  /**
   * 사용자의 마지막 로그인 시간입니다.
   */
  private LocalDateTime lastLoginAt;

  /**
   * 사용자 계정이 생성된 시간입니다.
   */
  private LocalDateTime createdAt;

  /**
   * 사용자 엔티티로부터 응답 객체를 생성하는 정적 팩토리 메서드
   */
  public static UserResponse from(User user) {
    return UserResponse.builder()
        .userId(user.getUserId())
        .email(user.getEmail())
        .name(user.getName())
        .notification(user.isNotification())
        .agreement(user.isAgreement())
        .firstLogin(user.isFirstLogin())
        .lastLoginAt(user.getLastLoginAt())
        .createdAt(user.getCreatedAt())
        .build();
  }
}