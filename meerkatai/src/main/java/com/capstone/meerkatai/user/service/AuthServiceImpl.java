package com.capstone.meerkatai.user.service;

import com.capstone.meerkatai.global.jwt.JwtUtil;
import com.capstone.meerkatai.storagespace.entity.StorageSpace;
import com.capstone.meerkatai.storagespace.repository.StorageSpaceRepository;
import com.capstone.meerkatai.user.dto.*;
import com.capstone.meerkatai.user.entity.User;
import com.capstone.meerkatai.user.entity.Role;
import com.capstone.meerkatai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
  private final StorageSpaceRepository storageSpaceRepository;

  /**
   * 기본 저장 공간 크기 (10GB)
   */
  private static final Long DEFAULT_STORAGE_SPACE = 10L * 1024 * 1024 * 1024; // 10GB in bytes

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

    // 사용자 저장
    User savedUser = userRepository.save(user);
    
    // 저장 공간 생성 및 할당 (기본 10GB)
    StorageSpace storageSpace = new StorageSpace();
    storageSpace.setUser(savedUser);
    storageSpace.setTotalSpace(DEFAULT_STORAGE_SPACE);
    storageSpace.setUsedSpace(0L);
    storageSpaceRepository.save(storageSpace);
    
    log.info("사용자 {} 생성 완료. 기본 저장 공간 {}GB 할당됨.", savedUser.getEmail(), DEFAULT_STORAGE_SPACE / (1024 * 1024 * 1024));

    return SignUpResponse.builder()
        .userId(savedUser.getUserId())
        .userEmail(savedUser.getEmail())
        .userName(savedUser.getName())
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

      Boolean notifyStatus;
      if (isFirstLogin) {
        user.setNotification(true);         // 알림 활성화
        userRepository.save(user);          // 저장
        notifyStatus = true;
      } else {
        notifyStatus = user.isNotification();
      }

      // 로그인 시간 업데이트
      user.updateLastLoginAt();
      
      // 저장 공간 정보 조회
      StorageSpace storageSpace = storageSpaceRepository.findByUserUserId(user.getUserId())
          .orElse(null);
      
      Long totalSpace = DEFAULT_STORAGE_SPACE; // 기본값 10GB
      Long usedSpace = 0L;
      
      if (storageSpace != null) {
          totalSpace = storageSpace.getTotalSpace() != null ? storageSpace.getTotalSpace() : DEFAULT_STORAGE_SPACE;
          usedSpace = storageSpace.getUsedSpace() != null ? storageSpace.getUsedSpace() : 0L;
      }

      return SignInResponse.builder()
          .token(token)
          .expiresIn(86400)
          .userId(user.getUserId())
          .userName(user.getName())
          .notifyStatus(user.isNotification())
          .firstLogin(isFirstLogin)
          .totalSpace(totalSpace)
          .usedSpace(usedSpace)
          .notifyStatus(notifyStatus)
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

    // 기존 비밀번호 확인
    if (!passwordEncoder.matches(request.getUserPassword(), user.getPassword())) {
      throw new BadCredentialsException("현재 비밀번호가 일치하지 않습니다.");
    }
    
    // 새 비밀번호가 현재 비밀번호와 동일한지 확인
    if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
      throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
    }

    // 새 비밀번호 암호화 및 저장
    String encodedPassword = passwordEncoder.encode(request.getNewPassword());
    user.setPassword(encodedPassword);
    
    log.info("사용자 {} 비밀번호 변경 완료", user.getEmail());
  }

  /**
   * 사용자 정보를 조회합니다.
   */
  @Override
  @Transactional(readOnly = true)
  public UserInfoResponse getUserInfo(Long userId) {
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
   * @return UpdateUserResponse 객체와 함께 성공 메시지를 반환합니다.
   */
  @Override
  @Transactional
  public UpdateUserResult updateUser(UpdateUserRequest request) {
    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    String message = null;
    boolean nameChanged = false;
    boolean passwordChanged = false;
    
    // 이름 변경 처리
    if (request.getUserName() != null && !request.getUserName().equals(user.getName())) {
      user.setName(request.getUserName());
      nameChanged = true;
    }
    
    // 비밀번호 변경 처리
    if (request.getNewPassword() != null) {
      // 현재 비밀번호가 제공되지 않은 경우
      if (request.getUserPassword() == null) {
        throw new IllegalArgumentException("비밀번호를 변경하려면 현재 비밀번호를 입력해야 합니다.");
      }
      
      // 현재 비밀번호 확인
      if (!passwordEncoder.matches(request.getUserPassword(), user.getPassword())) {
        throw new BadCredentialsException("현재 비밀번호가 일치하지 않습니다.");
      }
      
      // 새 비밀번호가 현재 비밀번호와 동일한지 확인
      if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
        throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
      }
      
      // 새 비밀번호 암호화 및 저장
      String encodedPassword = passwordEncoder.encode(request.getNewPassword());
      user.setPassword(encodedPassword);
      passwordChanged = true;
      
      log.info("사용자 ID {} 비밀번호 변경 완료", user.getUserId());
    }
    
    // 응답 메시지 생성
    if (nameChanged && passwordChanged) {
      message = "이름과 비밀번호가 성공적으로 변경되었습니다.";
    } else if (nameChanged) {
      message = "이름이 성공적으로 변경되었습니다.";
    } else if (passwordChanged) {
      message = "비밀번호가 성공적으로 변경되었습니다.";
    } else {
      message = "변경된 정보가 없습니다.";
    }

    // 응답 생성
    UpdateUserResponse response = UpdateUserResponse.builder()
        .userId(user.getUserId())
        .userName(user.getName())
        .updatedAt(ZonedDateTime.now())
        .build();
    
    return new UpdateUserResult(response, message);
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