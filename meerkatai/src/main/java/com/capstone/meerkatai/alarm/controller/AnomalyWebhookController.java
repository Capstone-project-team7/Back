package com.capstone.meerkatai.alarm.controller;

import com.capstone.meerkatai.alarm.dto.AnomalyVideoMetadataRequest;
import com.capstone.meerkatai.alarm.service.EmailService;

import com.capstone.meerkatai.anomalybehavior.entity.AnomalyBehavior;
import com.capstone.meerkatai.anomalybehavior.service.AnomalyBehaviorService;
import com.capstone.meerkatai.dashboard.service.DashboardService;
import com.capstone.meerkatai.storagespace.service.StorageSpaceService;
import com.capstone.meerkatai.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/anomaly")
@RequiredArgsConstructor
public class AnomalyWebhookController {

    private final EmailService emailService;
    private final AnomalyBehaviorService anomalyBehaviorService;
    private final VideoService videoService;
    private final DashboardService dashboardService;
    private final StorageSpaceService storageSpaceService;

//    이메일 발송
//    POST : http://localhost:8080/api/anomaly/notify
//    임의의 메타데이터 body
//    {
//        "videoUrl": "https://s3.amazonaws.com/your-bucket/video_20250430.mp4",
//            "anomalyType": "FALL",
//            "timestamp": "2025-04-30 17:33:21",
//            "userId": 3
//    }
    @PostMapping("/notify")
    public ResponseEntity<String> handleWebhook(@RequestBody AnomalyVideoMetadataRequest request) {
        //FastAPI에서 받은 메타데이터 이용해서 DB 저장 및 갱신
        // 1. 이상행동 DB 저장
        AnomalyBehavior savedBehavior = anomalyBehaviorService.saveAnomalyBehavior(request);

        // 2. 비디오 DB 저장
        videoService.saveVideo(request, savedBehavior);

        //3. 대시보드 DB 저장 OR 갱신
        dashboardService.updateDashboardWithAnomaly(request);

        //4. 저장공간 DB 갱신
        storageSpaceService.updateUsedSpace(request);

        //FastAPI에서 받은 메타 데이터 사용해서 이메일 발송하는 로직
        String result = emailService.processAndSendAnomalyEmail(request);

        // 응답 분기
        return switch (result) {
            case "User not found" -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            case "Notification is disabled for this user" -> ResponseEntity.ok(result);
            case "Failed to send email" -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            default -> ResponseEntity.ok(result);
        };
    }
}



