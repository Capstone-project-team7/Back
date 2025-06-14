package com.capstone.meerkatai.user.controller;

import com.capstone.meerkatai.user.dto.*;
import com.capstone.meerkatai.user.service.AuthService;
import com.capstone.meerkatai.user.entity.User;
import com.capstone.meerkatai.user.repository.UserRepository;
import com.capstone.meerkatai.video.dto.VideoDeleteRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


//사용자 인증 및 계정 관리를 위한 REST API 컨트롤러입니다.

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final UserRepository userRepository;

  /**
   * 새로운 사용자를 등록
   * @apiNote
   * 요청 예시:
   * <pre>
   * {
   *   "user_email": "user@example.com",
   *   "user_password": "password123",
   *   "user_name": "홍길동",
   *   "agreement_status": true
   * }
   * </pre>
   */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<SignUpResponse>> register(@Valid @RequestBody SignUpRequest request) {
    SignUpResponse response = authService.signup(request);
    return ResponseEntity.ok(new ApiResponse<>("success", response));
  }

  /**
   * 사용자 로그인을 처리
   * @apiNote
   * 요청 예시:
   * <pre>
   * {
   *   "user_email": "user@example.com",
   *   "user_password": "password123"
   * }
   * </pre>
   */
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<SignInResponse>> login(@Valid @RequestBody SignInRequest request) {
    SignInResponse response = authService.login(request);
    return ResponseEntity.ok(new ApiResponse<>("success", response));
  }

  /**
   * 사용자의 비밀번호를 재설정
   * @apiNote
   * 요청 예시:
   * <pre>
   * {
   *   "user_email": "user@example.com",
   *   "user_password": "currentpassword123",
   *   "new_password": "newpassword123"
   * }
   * </pre>
   */
  @PostMapping("/reset-password")
  public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request);
    return ResponseEntity.ok(new ApiResponse<>("success", "비밀번호가 성공적으로 변경되었습니다."));
  }

  /**
   * 현재 인증된 사용자의 상세 정보를 조회합니다.
   * JWT 토큰에서 추출한 사용자 이메일을 사용하여 해당 사용자 정보를 반환합니다.
   *
   * @return 현재 인증된 사용자의 정보가 포함된 ApiResponse 객체
   */
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUserInfo() {
    // 현재 인증된 사용자 정보 가져오기
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName(); // JWT에서 추출한 사용자 이메일

    // 이메일로 사용자 조회
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    // 사용자 정보 조회
    UserInfoResponse response = authService.getUserInfo(user.getUserId());
    return ResponseEntity.ok(new ApiResponse<>("success", response));
  }

  /**
   * 사용자 로그아웃을 처리
   * @apiNote
   * 요청 예시:
   * <pre>
   * {
   *   "user_id": 123
   * }
   * </pre>
   */
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<String>> logout(@Valid @RequestBody LogoutRequest request) {
    authService.logout(request);
    return ResponseEntity.ok(new ApiResponse<>("success", "로그아웃 되었습니다."));
  }

  /**
   * 사용자 정보를 수정
   * @apiNote
   * 요청 예시:
   * <pre>
   * {
   *   "user_id": 123,
   *   "user_name": "김철수",
   *   "user_password": "currentpassword",
   *   "new_password": "newpassword123"
   * }
   * </pre>
   * 
   * 응답 예시:
   * <pre>
   * {
   *   "status": "success",
   *   "data": {
   *     "user_id": 123,
   *     "user_name": "김철수",
   *     "updated_at": "2023-06-01T12:34:56.789Z"
   *   },
   *   "message": "비밀번호가 성공적으로 변경되었습니다."
   * }
   * </pre>
   */
  @PutMapping("/update")
  public ResponseEntity<ApiResponse<UpdateUserResponse>> updateUser(@Valid @RequestBody UpdateUserRequest request) {
    UpdateUserResult result = authService.updateUser(request);
    return ResponseEntity.ok(new ApiResponse<>("success", result.getResponse(), result.getMessage()));
  }

  /**
   * 회원 탈퇴를 처리
   * @apiNote
   * 요청 예시:
   * <pre>
   * {
   *   "user_id": 123,
   *   "user_password": "password123"
   * }
   * </pre>
   */

  // ✅ 공통 메서드: 현재 사용자 ID 조회
  private Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();

    return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."))
            .getUserId(); // User 엔티티에서 실제 ID 필드명에 맞게 수정
  }

//  @DeleteMapping("/withdraw")
//  public ResponseEntity<ApiResponse<String>> withdraw(@Valid @RequestBody WithdrawRequest request) {
//    authService.withdraw(request);
//    return ResponseEntity.ok(new ApiResponse<>("success", "회원 탈퇴가 완료되었습니다."));
//  }

  @DeleteMapping("/withdraw")
  public ResponseEntity<?> deleteUser(
          @Valid @RequestBody WithdrawRequest request
  ) {
    try {
      Long userId = getCurrentUserId();
      authService.withdraw(userId, request);

      return ResponseEntity.ok(Map.of(
              "status", "success",
              "message", "회원 탈퇴가 완료되었습니다."
      ));
    } catch (BadCredentialsException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
              "status", "error",
              "message", "비밀번호가 일치하지 않습니다."
      ));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
              "status", "error",
              "message", e.getMessage()
      ));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
              "status", "error",
              "message", "서버 내부 오류가 발생했습니다."
      ));
    }
  }

}