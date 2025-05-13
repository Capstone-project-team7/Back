package com.capstone.meerkatai.anomaly.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * FastAPI에서 전송하는 이상 감지 메타데이터를 담는 DTO
 */
@Getter
@ToString
@NoArgsConstructor
public class AnomalyVideoMetadataRequest {

    @JsonProperty("cctv_id")
    private Long cctvId;  // Long 또는 String으로 받을 수 있도록 Object 타입 사용

    @JsonProperty("videoUrl")
    private String videoUrl;

    @JsonProperty("anomalyType")
    private String anomalyType;

    @JsonProperty("confidence")
    private Double confidence;

    @JsonProperty("timestamp")
    // @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;

    @JsonProperty("user_id")
    private Long userId;
    
    /**
     * cctvId를 String으로 변환하여 반환
     */
    public String getCctvIdAsString() {
        return cctvId != null ? cctvId.toString() : null;
    }
    
    /**
     * cctvId를 Long으로 변환 시도
     * 변환 불가능한 경우 null 반환
     */
    public Long getCctvIdAsLong() {
        if (cctvId == null) {
            return null;
        }
        
        if (cctvId instanceof Number) {
            return ((Number) cctvId).longValue();
        }
        
        try {
            return Long.parseLong(cctvId.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
} 