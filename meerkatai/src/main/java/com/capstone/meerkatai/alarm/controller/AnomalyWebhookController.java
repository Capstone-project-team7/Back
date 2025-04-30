package com.capstone.meerkatai.alarm.controller;

import com.capstone.meerkatai.alarm.dto.AnomalyVideoMetadataRequest;
import com.capstone.meerkatai.alarm.service.EmailService;
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



