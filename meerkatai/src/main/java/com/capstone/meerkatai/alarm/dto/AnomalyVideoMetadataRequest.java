package com.capstone.meerkatai.alarm.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

// FastAPI에서 받을 이상행동 메타 데이터
@Getter
@NoArgsConstructor
public class AnomalyVideoMetadataRequest {
    private String videoUrl;
    private String anomalyType;
    private String timestamp;
    private Long userId;
}
