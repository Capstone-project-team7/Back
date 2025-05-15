package com.capstone.meerkatai.anomalybehavior.controller;

import com.capstone.meerkatai.anomalybehavior.entity.AnomalyBehavior;
import com.capstone.meerkatai.anomalybehavior.service.AnomalyBehaviorService;
import com.capstone.meerkatai.common.dto.ApiResponse;
import com.capstone.meerkatai.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/anomalies")
@RequiredArgsConstructor
public class AnomalyBehaviorController {

    private final AnomalyBehaviorService anomalyBehaviorService;

    /**
     * 사용자의 모든 이상행동 목록 조회
     * S3 URL을 presigned URL로 변환하여 제공
     * 
     * @param userId 사용자 ID
     * @return 이상행동 목록
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AnomalyBehavior>>> getUserAnomalies(@PathVariable Long userId) {
        try {
            List<AnomalyBehavior> anomalies = anomalyBehaviorService.getAllAnomalyBehaviorsWithPresignedUrls(userId);
            return ResponseEntity.ok(ApiResponse.success(anomalies));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 이상행동 상세 정보 조회
     * S3 URL을 presigned URL로 변환하여 제공
     * 
     * @param anomalyId 이상행동 ID
     * @return 이상행동 상세 정보
     */
    @GetMapping("/{anomalyId}")
    public ResponseEntity<ApiResponse<AnomalyBehavior>> getAnomalyDetail(@PathVariable Long anomalyId) {
        try {
            AnomalyBehavior anomaly = anomalyBehaviorService.getAnomalyBehaviorWithPresignedUrls(anomalyId);
            return ResponseEntity.ok(ApiResponse.success(anomaly));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}