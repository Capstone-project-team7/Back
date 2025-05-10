package com.capstone.meerkatai.anomalybehavior.service;

import com.capstone.meerkatai.alarm.dto.AnomalyVideoMetadataRequest;
import com.capstone.meerkatai.anomalybehavior.entity.AnomalyBehavior;
import com.capstone.meerkatai.anomalybehavior.repository.AnomalyBehaviorRepository;
import com.capstone.meerkatai.streamingvideo.entity.StreamingVideo;
import com.capstone.meerkatai.streamingvideo.repository.StreamingVideoRepository;
import com.capstone.meerkatai.user.entity.User;
import com.capstone.meerkatai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyBehaviorService {

    private final AnomalyBehaviorRepository anomalyBehaviorRepository;
    private final UserRepository userRepository;
    private final StreamingVideoRepository streamingVideoRepository;

    //FastAPI에서 받은 메타데이터 DB에 저장하는 메소드
    public AnomalyBehavior saveAnomalyBehavior(AnomalyVideoMetadataRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        StreamingVideo streamingVideo = streamingVideoRepository.findById(request.getCctvId())
                .orElseThrow(() -> new RuntimeException("스트리밍 비디오 없음"));

        AnomalyBehavior behavior = AnomalyBehavior.builder()
                .anomalyBehaviorType(request.getAnomalyType())
                .anomalyTime(request.getTimestamp())
                .anomalyVideoLink(request.getVideoUrl())
                .anomalyThumbnailLink(request.getThumbnailUrl())
                .streamingVideo(streamingVideo)
                .user(user)
                .build();

        anomalyBehaviorRepository.save(behavior);


        AnomalyBehavior saved = anomalyBehaviorRepository.save(behavior);
        log.info("✅ 이상행동 저장 완료: anomaly_id={}", saved.getAnomalyId());
        return saved;
    }
}
