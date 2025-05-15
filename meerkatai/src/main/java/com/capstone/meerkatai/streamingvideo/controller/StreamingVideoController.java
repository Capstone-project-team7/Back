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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/streaming-video")
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
   * ✅ 사용자가 활성화 버튼 클릭 시:
   * - 해당 ｃｃｔｖ RTSP 연결 시도
   * － 해당 ＣＣＴＶＩＤ 아닌 ｃｃｔｖ 비활성화 상태로 ＤＢ에 저장 및 ＦａｓｔＡＰＩ에 스트리밍 중지 요청
   * - 해당 ｃｃｔｖ RTSP 연결 가능하면 StreamingVideo 테이블 생성
   * - FastAPI에 사용자 + CCTV 정보 전송 및 스트리밍 연결 요청
   */

  @PutMapping("/connect/{cctvId}")
  public ResponseEntity<Map<String, String>> connectToCctv(@PathVariable Long cctvId) {
    //사용자 조회
    Long userId = getCurrentUserId();

    // 1. CCTV 엔티티 조회
    Cctv cctv = cctvRepository.findByCctvIdAndUserUserId(cctvId, userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자의 CCTV를 찾을 수 없습니다."));

    // 2. RTSP URL 생성
//    String rtspUrl = String.format("rtsp://%s:%s@%s/%s",
//            cctv.getCctvAdmin(),
//            cctv.getCctvPassword(),
//            cctv.getIpAddress(),
//            cctv.getCctvPath()
//    );

    String rtspUrl = String.format("rtsp://%s:%s@%s:%d/%s",
            cctv.getCctvAdmin(),
            cctv.getCctvPassword(),
            cctv.getIpAddress(),
            1945, // 하드코딩된 포트
            cctv.getCctvPath()
    );


    // 3. 연결 테스트 + 저장 + FastAPI 전송
    boolean connected = streamingVideoService.connectAndRegister(userId, cctvId, rtspUrl);

    Map<String, String> response = new HashMap<>();
    if (connected) {
      response.put("status", "success");
      response.put("message", "RTSP 연결 성공 및 연동 완료");
      return ResponseEntity.ok(response);
    } else {
      response.put("status", "fail");
      response.put("message", "RTSP 연결 실패");
      return ResponseEntity.status(500).body(response);
    }
  }

  /**
   * ✅ 사용자가 버튼 클릭 시 스트리밍 중지
   * - StreamingVideo 상태 false로 변경
   * - FastAPI로 중지 요청 전달
   */
  @PutMapping("/disconnect/{cctvId}")
  public ResponseEntity<Map<String, String>> disconnectFromCctv(@PathVariable Long cctvId) {
    Long userId = getCurrentUserId();  // 현재 로그인된 사용자

    boolean disconnected = streamingVideoService.disconnectAndNotify(userId, cctvId);

    Map<String, String> response = new HashMap<>();
    if (disconnected) {
      response.put("status", "success");
      response.put("message", "스트리밍 중지 완료");
      return ResponseEntity.ok(response);
    } else {
      response.put("status", "fail");
      response.put("message", "스트리밍 중지 실패 (FastAPI 요청 실패)");
      return ResponseEntity.status(500).body(response);
    }
  }
}

