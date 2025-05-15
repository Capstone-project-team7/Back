package com.capstone.meerkatai.anomalybehavior.service;

import com.capstone.meerkatai.alarm.dto.AnomalyVideoMetadataRequest;
import com.capstone.meerkatai.anomalybehavior.entity.AnomalyBehavior;
import com.capstone.meerkatai.anomalybehavior.repository.AnomalyBehaviorRepository;
import com.capstone.meerkatai.cctv.entity.Cctv;
import com.capstone.meerkatai.cctv.repository.CctvRepository;
import com.capstone.meerkatai.global.service.S3Service;
import com.capstone.meerkatai.streamingvideo.entity.StreamingVideo;
import com.capstone.meerkatai.streamingvideo.repository.StreamingVideoRepository;
import com.capstone.meerkatai.user.entity.User;
import com.capstone.meerkatai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyBehaviorService {

    private final AnomalyBehaviorRepository anomalyBehaviorRepository;
    private final UserRepository userRepository;
    private final StreamingVideoRepository streamingVideoRepository;
    private final CctvRepository cctvRepository;
    private final S3Service s3Service;

    //FastAPI에서 받은 메타데이터 DB에 저장하는 메소드
    @Transactional
    public AnomalyBehavior saveAnomalyBehavior(AnomalyVideoMetadataRequest request) {
        log.info("이상행동 저장 시작: 사용자ID={}, CCTVID={}", request.getUserId(), request.getCctvId());
        
        // 사용자 조회
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자 없음: ID=" + request.getUserId()));
        log.debug("사용자 조회 성공: {}", user.getUserId());

        // StreamingVideo 조회 - CCTV ID로 조회 시도
        StreamingVideo streamingVideo;
        try {
            // 1. 사용자ID와 CCTVID로 먼저 조회 시도
            Optional<StreamingVideo> streamingVideoOpt = streamingVideoRepository
                    .findByUserUserIdAndCctvCctvId(request.getUserId(), request.getCctvId());
            
            if (streamingVideoOpt.isPresent()) {
                streamingVideo = streamingVideoOpt.get();
                log.info("사용자ID와 CCTVID로 StreamingVideo 조회 성공: id={}", streamingVideo.getStreamingVideoId());
            } else {
                // 2. CCTV ID로만 조회 시도
                List<StreamingVideo> streamingVideos = streamingVideoRepository.findByCctvCctvId(request.getCctvId());
                
                if (!streamingVideos.isEmpty()) {
                    streamingVideo = streamingVideos.get(0); // 가장 첫 번째 항목 사용
                    log.info("CCTVID로 StreamingVideo 조회 성공: id={}", streamingVideo.getStreamingVideoId());
                } else {
                    // 3. 조회 실패 시 새로 생성
                    log.warn("StreamingVideo를 찾을 수 없어 새로 생성합니다: cctvId={}", request.getCctvId());
                    
                    // CCTV 엔티티 조회
                    Cctv cctv = cctvRepository.findById(request.getCctvId())
                            .orElseThrow(() -> new RuntimeException("CCTV를 찾을 수 없습니다: ID=" + request.getCctvId()));
                    
                    // StreamingVideo 생성
                    streamingVideo = StreamingVideo.builder()
                            .user(user)
                            .cctv(cctv)
                            .startTime(LocalDateTime.now())
                            .streamingVideoStatus(false)
                            .streamingUrl("rtsp://placeholder") // 임시 URL
                            .build();
                    
                    streamingVideo = streamingVideoRepository.save(streamingVideo);
                    log.info("새 StreamingVideo 생성 완료: id={}", streamingVideo.getStreamingVideoId());
                }
            }
        } catch (Exception e) {
            log.error("StreamingVideo 처리 중 오류 발생", e);
            throw new RuntimeException("StreamingVideo 처리 오류: " + e.getMessage(), e);
        }

        // S3 URL 확인 및 처리
        String videoUrl = request.getVideoUrl();
        String thumbnailUrl = request.getThumbnailUrl();
        
        // S3 URL 검증
        if (videoUrl != null && !s3Service.isS3Url(videoUrl)) {
            log.warn("비디오 URL이 S3 URL 형식이 아닙니다: {}", videoUrl);
        }
        
        // 썸네일 URL 처리
        if (thumbnailUrl == null || thumbnailUrl.trim().isEmpty()) {
            if (videoUrl != null && !videoUrl.trim().isEmpty()) {
                // 비디오 URL에서 썸네일 URL 생성
                thumbnailUrl = s3Service.generateThumbnailUrlFromVideoUrl(videoUrl);
                log.info("비디오 URL에서 썸네일 URL 생성: {}", thumbnailUrl);
            }
        }

        // AnomalyBehavior 생성 및 저장
        try {
            AnomalyBehavior behavior = AnomalyBehavior.builder()
                    .anomalyBehaviorType(request.getAnomalyType())
                    .anomalyTime(request.getTimestamp())
                    .anomalyVideoLink(videoUrl)
                    .anomalyThumbnailLink(thumbnailUrl)
                    .streamingVideo(streamingVideo)
                    .user(user)
                    .build();

            AnomalyBehavior saved = anomalyBehaviorRepository.save(behavior);
            log.info("✅ 이상행동 저장 완료: anomaly_id={}", saved.getAnomalyId());
            return saved;
        } catch (Exception e) {
            log.error("이상행동 저장 중 오류 발생", e);
            throw new RuntimeException("이상행동 저장 오류: " + e.getMessage(), e);
        }
    }
    
    /**
     * 사용자의 모든 이상행동 목록 조회 (S3 URL을 presigned URL로 변환)
     */
    public List<AnomalyBehavior> getAllAnomalyBehaviorsWithPresignedUrls(Long userId) {
        // 사용자의 이상행동 목록 조회 (JPA 메소드명 패턴에 맞게 수정)
        List<AnomalyBehavior> anomalyBehaviors = anomalyBehaviorRepository.findByUserUserId(userId);
        
        // URL을 presigned URL로 변환하여 반환
        return anomalyBehaviors.stream()
                .map(behavior -> {
                    // S3 URL을 presigned URL로 변환
                    String videoLink = generatePresignedUrlIfNeeded(behavior.getAnomalyVideoLink());
                    String thumbnailLink = generatePresignedUrlIfNeeded(behavior.getAnomalyThumbnailLink());
                    
                    // 객체의 URL을 presigned URL로 변경
                    behavior.setAnomalyVideoLink(videoLink);
                    behavior.setAnomalyThumbnailLink(thumbnailLink);
                    return behavior;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 이상행동 상세 정보 조회 (S3 URL을 presigned URL로 변환)
     */
    public AnomalyBehavior getAnomalyBehaviorWithPresignedUrls(Long anomalyId) {
        AnomalyBehavior behavior = anomalyBehaviorRepository.findById(anomalyId)
                .orElseThrow(() -> new RuntimeException("이상행동 정보를 찾을 수 없습니다: ID=" + anomalyId));
        
        // S3 URL을 presigned URL로 변환
        String videoLink = generatePresignedUrlIfNeeded(behavior.getAnomalyVideoLink());
        String thumbnailLink = generatePresignedUrlIfNeeded(behavior.getAnomalyThumbnailLink());
        
        // 객체의 URL을 presigned URL로 변경
        behavior.setAnomalyVideoLink(videoLink);
        behavior.setAnomalyThumbnailLink(thumbnailLink);
        
        return behavior;
    }
    
    /**
     * S3 URL인 경우 Presigned URL로 변환, 아닌 경우 원래 URL 반환
     */
    private String generatePresignedUrlIfNeeded(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        
        try {
            if (s3Service.isS3Url(url)) {
                String objectKey = s3Service.extractS3Key(url);
                if (objectKey != null) {
                    URL presignedUrl = s3Service.generatePresignedUrlForDownload(objectKey);
                    return presignedUrl.toString();
                }
            }
        } catch (Exception e) {
            log.warn("Presigned URL 생성 실패, 원본 URL 사용: {}", e.getMessage());
        }
        
        return url;
    }
}
