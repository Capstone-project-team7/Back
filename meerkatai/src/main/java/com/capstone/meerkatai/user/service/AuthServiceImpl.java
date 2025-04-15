package com.capstone.meerkatai.user.service;

import com.capstone.meerkatai.global.jwt.JwtUtil;
import com.capstone.meerkatai.user.dto.*;
import com.capstone.meerkatai.user.entity.User;
import com.capstone.meerkatai.user.entity.Role;
import com.capstone.meerkatai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

/**
 * 인증 및 사용자 관리 서비스의 구현체
 *
 * @see com.capstone.meerkatai.user.service.AuthService
 * @see com.capstone.meerkatai.global.jwt.JwtUtil
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;

  /**
   * 회원가입을 처리합니다.
   */
  @Override
  @Transactional
  public SignUpResponse signup(SignUpRequest request) {
    // 이메일 중복 검사
    if (userRepository.existsByEmail(request.getUserEmail())) {
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
    }

    // 비밀번호 암호화
    String encodedPassword = passwordEncoder.encode(request.getUserPassword());

    // 사용자 엔티티 생성
    User user = User.builder()
        .email(request.getUserEmail())
        .password(encodedPassword)
        .name(request.getUserName())
        .agreement(request.getAgreementStatus())
        .notification(true)
        .firstLogin(true)
        .role(Role.USER)
        .build();

    userRepository.save(user);

    return SignUpResponse.builder()
        .userId(user.getUserId())
        .userEmail(user.getEmail())
        .userName(user.getName())
        .build();
  }

  /**
   * 로그인을 처리합니다.
   */
  @Override
  @Transactional
  public SignInResponse login(SignInRequest request) {
    try {
      // 인증
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getUserEmail(), request.getUserPassword())
      );

      // 사용자 정보 조회
      User user = userRepository.findByEmail(request.getUserEmail())
          .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

      // 현재 firstLogin 상태 저장
      boolean isFirstLogin = user.isFirstLogin();

      // JWT 토큰 생성
      String token = jwtUtil.generateToken(user.getEmail());

      // 로그인 시간 업데이트
      user.updateLastLoginAt();

      return SignInResponse.builder()
          .token(token)
          .expiresIn(86400)
          .userId(user.getUserId())
          .firstLogin(isFirstLogin)
          .build();
    } catch (BadCredentialsException e) {
      throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
    } catch (Exception e) {
      throw new RuntimeException("로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  /**
   * 비밀번호 재설정을 처리합니다.
   */
  @Override
  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    User user = userRepository.findByEmail(request.getUserEmail())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

    // 새 비밀번호 암호화 및 저장
    String encodedPassword = passwordEncoder.encode(request.getNewPassword());
    user.setPassword(encodedPassword);
  }

  /**
   * 사용자 정보를 조회합니다.
   */
  @Override
  @Transactional(readOnly = true)
  public UserInfoResponse getUserInfo(Integer userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    return UserInfoResponse.builder()
        .userId(user.getUserId())
        .userEmail(user.getEmail())
        .userName(user.getName())
        .notifyStatus(user.isNotification())
        .agreementStatus(user.isAgreement())
        .firstLogin(user.isFirstLogin())
        .build();
  }

  /**
   * 로그아웃을 처리합니다.
   */
  @Override
  @Transactional
  public void logout(LogoutRequest request) {
    // 클라이언트에서 토큰을 삭제하므로 서버에서는 특별한 처리가 필요없음
    // 추후 필요시 로그아웃 시간 기록 등의 작업을 추가할 수 있음
  }

  /**
   * 사용자 정보를 수정합니다.
   */
  @Override
  @Transactional
  public UpdateUserResponse updateUser(UpdateUserRequest request) {
    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    if (request.getUserName() != null) {
      user.setName(request.getUserName());
    }
    if (request.getUserPassword() != null) {
      user.setPassword(passwordEncoder.encode(request.getUserPassword()));
    }
    if (request.getNotifyStatus() != null) {
      user.setNotification(request.getNotifyStatus());
    }

    return UpdateUserResponse.builder()
        .userId(user.getUserId())
        .userName(user.getName())
        .notifyStatus(user.isNotification())
        .updatedAt(ZonedDateTime.now())
        .build();
  }

  /**
   * 회원 탈퇴를 처리합니다.
   */
  @Override
  @Transactional
  public void withdraw(WithdrawRequest request) {
    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    if (!passwordEncoder.matches(request.getUserPassword(), user.getPassword())) {
      throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
    }

    userRepository.delete(user);
  }
}