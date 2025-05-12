package com.capstone.meerkatai.alarm.controller;

import com.capstone.meerkatai.alarm.dto.AnomalyVideoMetadataRequest;
import com.capstone.meerkatai.alarm.dto.AnomalyVideoResponse;
import com.capstone.meerkatai.alarm.dto.UserNotificationUpdateRequest;
import com.capstone.meerkatai.alarm.service.EmailService;
import com.capstone.meerkatai.global.service.S3Service;
import com.capstone.meerkatai.user.entity.User;
import com.capstone.meerkatai.anomalybehavior.entity.AnomalyBehavior;
import com.capstone.meerkatai.anomalybehavior.service.AnomalyBehaviorService;
import com.capstone.meerkatai.dashboard.service.DashboardService;
import com.capstone.meerkatai.storagespace.service.StorageSpaceService;
import com.capstone.meerkatai.user.repository.UserRepository;
import com.capstone.meerkatai.user.service.UserService;
import com.capstone.meerkatai.video.entity.Video;
import com.capstone.meerkatai.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URL;

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
    private final S3Service s3Service;
    
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

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
        Video savedVideo = videoService.saveVideo(request, savedBehavior);

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
     * ✅ 이상행동 영상 업로드를 위한 Pre-signed URL 발급
     * 영상과 썸네일 업로드를 위한 URL을 생성합니다.
     * 
     * POST /api/anomaly/upload-urls
     * Body: {
     *   "cctv_id": 456,
     *   "anomalyType": "FALL",
     *   "timestamp": "2025-05-08T19:17:05",
     *   "user_id": 3
     * }
     * 
     * Response: {
     *   "video_url": "https://s3.amazonaws.com/...",
     *   "thumbnail_url": "https://s3.amazonaws.com/...",
     *   "video_key": "clips/456_20250508_191705.mp4",
     *   "thumbnail_key": "thumbnails/456_20250508_191705.jpg"
     * }
     */
    @PostMapping("/anomaly/upload-urls")
    public ResponseEntity<AnomalyVideoResponse> getUploadUrls(@RequestBody AnomalyVideoMetadataRequest request) {
        // S3 객체 키 생성
        String videoKey = s3Service.generateVideoKey(request.getCctvId());
        String thumbnailKey = s3Service.generateThumbnailKey(videoKey);
        
        // S3 업로드용 pre-signed URL 생성
        URL videoUploadUrl = s3Service.generatePresignedUrlForUpload(videoKey);
        URL thumbnailUploadUrl = s3Service.generatePresignedUrlForUpload(thumbnailKey);
        
        // 응답 생성
        AnomalyVideoResponse response = new AnomalyVideoResponse(
            videoUploadUrl.toString(),
            thumbnailUploadUrl.toString(),
            videoKey,
            thumbnailKey
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ 사용자 알림 수신 설정 변경
     * PUT /api/v1/user/notification
     * Body: { "notification": true }
     */
    @PutMapping("/v1/user/notification")
    public ResponseEntity<String> updateNotificationStatus(@RequestBody UserNotificationUpdateRequest request) {
        Long userId = getCurrentUserId();

        // 알림 설정 업데이트
        userService.updateNotificationStatus(userId, request.isNotification());

        return ResponseEntity.ok("알림 설정이 업데이트되었습니다.");
    }
}



