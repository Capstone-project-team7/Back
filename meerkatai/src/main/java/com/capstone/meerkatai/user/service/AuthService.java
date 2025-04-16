package com.capstone.meerkatai.user.service;

import com.capstone.meerkatai.user.dto.*;

/**
 * 인증 및 사용자 관리를 위한 서비스 인터페이스입니다.
 */
public interface AuthService {

  /**
   * 사용자 회원가입을 처리하는 메서드입니다.
   *
   * @param request 회원가입 요청 정보
   * @return 회원가입 완료 후 사용자 정보
   * @throws IllegalArgumentException 이미 사용 중인 이메일인 경우
   */
  SignUpResponse signup(SignUpRequest request);

  /**
   * 사용자 로그인을 처리하는 메서드입니다.
   */
  SignInResponse login(SignInRequest request);

  /**
   * 비밀번호 재설정을 처리하는 메서드입니다.
   */
  void resetPassword(ResetPasswordRequest request);

  /**
   * 사용자 정보를 조회하는 메서드입니다.
   */
  UserInfoResponse getUserInfo(Integer userId);

  /**
   * 사용자 로그아웃을 처리하는 메서드입니다.
   *
   * @param request 로그아웃 요청 정보
   */
  void logout(LogoutRequest request);

  /**
   * 사용자 정보를 수정하는 메서드입니다.
   *
   * @param request 사용자 정보 수정 요청 정보
   * @return 수정된 사용자 정보
   * @throws IllegalArgumentException 존재하지 않는 사용자인 경우
   */
  UpdateUserResponse updateUser(UpdateUserRequest request);

  /**
   * 회원 탈퇴를 처리하는 메서드입니다.
   *
   * @param request 회원 탈퇴 요청 정보
   * @throws IllegalArgumentException 존재하지 않는 사용자인 경우
   * @throws org.springframework.security.authentication.BadCredentialsException 비밀번호가 일치하지 않는 경우
   */
  void withdraw(WithdrawRequest request);
}