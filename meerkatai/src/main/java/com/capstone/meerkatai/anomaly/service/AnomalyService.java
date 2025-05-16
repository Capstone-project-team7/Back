package com.capstone.meerkatai.anomaly.service;

import com.capstone.meerkatai.anomaly.dto.AnomalyVideoMetadataRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이상 감지 관련 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyService {

    /**
     * 이상 감지 데이터 처리
     * @param request 이상 감지 메타데이터 요청 객체
     */
    @Transactional
    public void processAnomalyDetection(AnomalyVideoMetadataRequest request) {
        log.info("이상 감지 데이터 처리 시작");
        
        // 1. CCTV ID 처리 - String 또는 Long 타입으로 변환
        Object cctvIdRaw = request.getCctvId();
        Long cctvIdAsLong = request.getCctvIdAsLong();
        String cctvIdAsString = request.getCctvIdAsString();
        
        log.info("CCTV ID 정보: raw={}, asLong={}, asString={}", 
                cctvIdRaw, cctvIdAsLong, cctvIdAsString);
        
        // 2. 비디오 URL 및 썸네일 URL 처리
        String videoUrl = request.getVideoUrl();
        String thumbnailUrl = request.getThumbnailUrl();
        
        log.info("미디어 URL 정보: video={}, thumbnail={}", videoUrl, thumbnailUrl);
        
        // 3. 이상행동 유형 및 신뢰도 처리
        String anomalyType = request.getAnomalyType();
        Double confidence = request.getConfidence();
        
        log.info("이상행동 정보: type={}, confidence={}", anomalyType, confidence);
        
        // 여기서 실제 비즈니스 로직 구현
        // - 데이터베이스 저장
        // - 알림 발송
        // - 기타 처리
        
        log.info("이상 감지 데이터 처리 완료");
    }
} 