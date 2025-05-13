package com.capstone.meerkatai.anomaly.controller;

import com.capstone.meerkatai.anomaly.dto.AnomalyVideoMetadataRequest;
import com.capstone.meerkatai.anomaly.dto.ApiResponse;
import com.capstone.meerkatai.anomaly.service.AnomalyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnomalyController {

    private final AnomalyService anomalyService;

    /**
     * FastAPI로부터 이상 감지 데이터를 수신하는 엔드포인트
     * URL: POST /api/anomaly/detection
     */
    @PostMapping("/anomaly/detection")
    public ResponseEntity<ApiResponse<String>> handleAnomalyNotification(
            @RequestBody AnomalyVideoMetadataRequest request) {
        
        log.info("이상 감지 데이터 수신: {}", request);
        
        try {
            // 서비스를 통해 데이터 처리
            anomalyService.processAnomalyDetection(request);
            
            // 성공 응답 반환
            return ResponseEntity.ok(ApiResponse.success("이상 감지 데이터가 성공적으로 처리되었습니다."));
        } catch (Exception e) {
            log.error("이상 감지 데이터 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("요청을 처리하는 중 오류가 발생했습니다."));
        }
    }
} 