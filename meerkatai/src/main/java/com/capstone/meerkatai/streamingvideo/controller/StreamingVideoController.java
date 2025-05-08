package com.capstone.meerkatai.streamingvideo.controller;

import com.capstone.meerkatai.cctv.entity.Cctv;
import com.capstone.meerkatai.cctv.repository.CctvRepository;
import com.capstone.meerkatai.streamingvideo.service.StreamingVideoService;
import com.capstone.meerkatai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/streaming-videos")
@RequiredArgsConstructor
public class StreamingVideoController {

  private final StreamingVideoService streamingVideoService;
  private final UserRepository userRepository;
  private final CctvRepository cctvRepository;

  private Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();

    return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."))
            .getUserId();
  }

  /**
   * ✅ 사용자가 버튼 클릭 시:
   * - RTSP 연결 시도
   * - 연결 가능하면 StreamingVideo 테이블 생성
   * - FastAPI에 사용자 + CCTV 정보 전송
   */

  @PostMapping("/connect")
  public ResponseEntity<String> connectToCctv(@RequestParam Long cctvId) {
    //사용자 조회
    Long userId = getCurrentUserId();

    // 1. CCTV 엔티티 조회
    Cctv cctv = cctvRepository.findByCctvIdAndUserUserId(cctvId, userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자의 CCTV를 찾을 수 없습니다."));

    // 2. RTSP URL 생성
    String rtspUrl = String.format("rtsp://%s:%s@%s/%s",
            cctv.getCctvAdmin(),
            cctv.getCctvPassword(),
            cctv.getIpAddress(),
            cctv.getCctvPath()
    );

    // 3. 연결 테스트 + 저장 + FastAPI 전송
    boolean connected = streamingVideoService.connectAndRegister(userId, cctvId, rtspUrl);

    return connected
            ? ResponseEntity.ok("✅ RTSP 연결 성공 및 연동 완료")
            : ResponseEntity.status(500).body("❌ RTSP 연결 실패");
  }
}

