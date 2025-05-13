package com.capstone.meerkatai.alarm.controller;

import com.capstone.meerkatai.alarm.dto.AnomalyVideoMetadataRequest;
import com.capstone.meerkatai.alarm.dto.UserNotificationUpdateRequest;
import com.capstone.meerkatai.alarm.service.EmailService;
import com.capstone.meerkatai.user.entity.User;
import com.capstone.meerkatai.anomalybehavior.entity.AnomalyBehavior;
import com.capstone.meerkatai.anomalybehavior.service.AnomalyBehaviorService;
import com.capstone.meerkatai.common.dto.ApiResponse;
import com.capstone.meerkatai.common.exception.ResourceNotFoundException;
import com.capstone.meerkatai.dashboard.service.DashboardService;
import com.capstone.meerkatai.storagespace.service.StorageSpaceService;
import com.capstone.meerkatai.user.repository.UserRepository;
import com.capstone.meerkatai.user.service.UserService;
import com.capstone.meerkatai.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnomalyWebhookController {

    private final EmailService emailService;
    private final AnomalyBehaviorService anomalyBehaviorService;
    private final VideoService videoService;
    private final DashboardService dashboardService;
    private final StorageSpaceService storageSpaceService;
    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * ✅ 현재 로그인된 사용자 ID 추출
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email))
                .getUserId();
    }

    /**
     * FastAPI로부터 이상행동 감지 데이터를 수신하는 Webhook 엔드포인트
     * POST : http://localhost:8080/api/anomaly/notify
     * 
     * @param request 이상행동 비디오 메타데이터 요청 객체
     * @return 처리 결과를 담은 ApiResponse
     */
    @PostMapping("/anomaly/notify")
    public ResponseEntity<ApiResponse<String>> handleWebhook(@RequestBody AnomalyVideoMetadataRequest request) {
        log.info("이상 행동 감지 데이터 수신: {}", request);
        
        try {
            // 필수 필드 검증
            validateRequest(request);
            
            // 0. 사용자 정보 조회
            User user = userService.getUserById(request.getUserId());
            if (user == null) {
                log.error("사용자 ID를 찾을 수 없음: {}", request.getUserId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found with id: " + request.getUserId()));
            }

            //FastAPI에서 받은 메타데이터 이용해서 DB 저장 및 갱신
            try {
                // 1. 이상행동 DB 저장
                AnomalyBehavior savedBehavior = anomalyBehaviorService.saveAnomalyBehavior(request);
                log.info("이상행동 저장 완료: behaviorId={}", savedBehavior.getAnomalyId());

                // 2. 비디오 DB 저장
                videoService.saveVideo(request, savedBehavior);
                log.info("비디오 저장 완료");

                //3. 대시보드 DB 저장 OR 갱신
                dashboardService.updateDashboardWithAnomaly(request);
                log.info("대시보드 업데이트 완료");

                //4. 저장공간 DB 갱신
                storageSpaceService.updateUsedSpace(request);
                log.info("저장공간 업데이트 완료");
            } catch (Exception e) {
                log.error("데이터 처리 중 오류 발생", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("데이터 저장 중 오류 발생: " + e.getMessage()));
            }

            // ✅ notification 여부 확인 후 이메일 전송
            if (!user.isNotification()) {
                log.info("사용자 알림 설정이 비활성화됨: userId={}", user.getUserId());
                return ResponseEntity.ok(ApiResponse.success("Notification is disabled for this user"));
            }

            // 이메일 발송
            try {
                String result = emailService.processAndSendAnomalyEmail(request);
                
                // 응답 분기
                if ("Failed to send email".equals(result)) {
                    log.error("이메일 발송 실패");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("이메일 발송 중 오류가 발생했습니다."));
                } else {
                    log.info("이메일 발송 성공: userId={}, email={}", user.getUserId(), user.getEmail());
                    return ResponseEntity.ok(ApiResponse.success("이상행동 데이터가 성공적으로 처리되었으며 이메일 알림이 발송되었습니다."));
                }
            } catch (Exception e) {
                log.error("이메일 발송 중 오류 발생", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("이메일 발송 중 오류 발생: " + e.getMessage()));
            }
        } catch (IllegalArgumentException e) {
            log.error("요청 데이터 검증 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("알 수 없는 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("요청 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 요청 객체의 필수 필드를 검증합니다.
     * 
     * @param request 검증할 요청 객체
     * @throws IllegalArgumentException 필수 필드가 누락된 경우
     */
    private void validateRequest(AnomalyVideoMetadataRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("사용자 ID(user_id)는 필수 항목입니다.");
        }
        
        if (request.getVideoUrl() == null || request.getVideoUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("비디오 URL(videoUrl)은 필수 항목입니다.");
        }
        
        if (request.getAnomalyType() == null || request.getAnomalyType().trim().isEmpty()) {
            throw new IllegalArgumentException("이상행동 유형(anomalyType)은 필수 항목입니다.");
        }
        
        if (request.getTimestamp() == null) {
            throw new IllegalArgumentException("타임스탬프(timestamp)는 필수 항목입니다.");
        }
    }

    /**
     * ✅ 사용자 알림 수신 설정 변경
     * PUT /api/v1/user/notification
     * Body: { "notification": true }
     */
    @PutMapping("/v1/user/notification")
    public ResponseEntity<ApiResponse<String>> updateNotificationSetting(@RequestBody UserNotificationUpdateRequest request) {
        try {
            Long userId = getCurrentUserId();
            log.info("사용자 알림 설정 변경 요청: userId={}, notification={}", userId, request.isNotification());

            boolean updated = userService.updateNotificationStatus(userId, request.isNotification());

            if (updated) {
                log.info("사용자 알림 설정 변경 성공: userId={}", userId);
                return ResponseEntity.ok(ApiResponse.success("✅ 알림 수신 설정이 업데이트되었습니다."));
            } else {
                log.warn("사용자 알림 설정 변경 실패: userId={}", userId);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("❌ 사용자 알림 설정 변경 실패"));
            }
        } catch (ResourceNotFoundException e) {
            log.error("사용자를 찾을 수 없음", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("알림 설정 변경 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("알림 설정 변경 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}



