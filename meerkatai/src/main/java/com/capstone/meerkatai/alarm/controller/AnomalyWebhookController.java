package com.capstone.meerkatai.alarm.controller;

import com.capstone.meerkatai.alarm.dto.AnomalyVideoMetadataRequest;
import com.capstone.meerkatai.alarm.dto.UserNotificationUpdateRequest;
import com.capstone.meerkatai.alarm.service.EmailService;
import com.capstone.meerkatai.user.entity.User;
import com.capstone.meerkatai.anomalybehavior.entity.AnomalyBehavior;
import com.capstone.meerkatai.anomalybehavior.service.AnomalyBehaviorService;
import com.capstone.meerkatai.dashboard.service.DashboardService;
import com.capstone.meerkatai.storagespace.service.StorageSpaceService;
import com.capstone.meerkatai.user.repository.UserRepository;
import com.capstone.meerkatai.user.service.UserService;
import com.capstone.meerkatai.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."))
                .getUserId();
    }

//    이메일 발송
//    POST : http://localhost:8080/api/anomaly/notify
//    임의의 메타데이터 body
//    {
//        "videoUrl": "https://s3.amazonaws.com/your-bucket/video_20250430.mp4",
//            "anomalyType": "FALL",
//            "timestamp": "2025-04-30 17:33:21",
//            "userId": 3
//    }
    @PostMapping("/anomaly/notify")
    public ResponseEntity<String> handleWebhook(@RequestBody AnomalyVideoMetadataRequest request) {
        // 0. 사용자 정보 조회
        User user = userService.getUserById(request.getUserId());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        //FastAPI에서 받은 메타데이터 이용해서 DB 저장 및 갱신
        // 1. 이상행동 DB 저장
        AnomalyBehavior savedBehavior = anomalyBehaviorService.saveAnomalyBehavior(request);

        // 2. 비디오 DB 저장
        videoService.saveVideo(request, savedBehavior);

        //3. 대시보드 DB 저장 OR 갱신
        dashboardService.updateDashboardWithAnomaly(request);

        //4. 저장공간 DB 갱신
        storageSpaceService.updateUsedSpace(request);

        // ✅ notification 여부 확인 후 이메일 전송
        if (!user.isNotification()) {
            return ResponseEntity.ok("Notification is disabled for this user");
        }

        // 이메일 발송
        String result = emailService.processAndSendAnomalyEmail(request);

        // 응답 분기
        return switch (result) {
            case "Failed to send email" -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            default -> ResponseEntity.ok(result);
        };
    }

    /**
     * ✅ 사용자 알림 수신 설정 변경
     * PUT /api/v1/user/notification
     * Body: { "notification": true }
     */
    @PutMapping("/v1/user/notification")
    public ResponseEntity<Map<String, String>> updateNotificationSetting(@RequestBody UserNotificationUpdateRequest request) {
        Long userId = getCurrentUserId();

        boolean updated = userService.updateNotificationStatus(userId, request.isNotification());

        Map<String, String> response = new HashMap<>();
        if (updated) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "fail");
            return ResponseEntity.badRequest().body(response);
        }
    }
}



