package com.capstone.meerkatai.alarm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이상행동 영상 업로드를 위한 Pre-signed URL 응답 DTO
 * <p>
 * 클라이언트가 이 URL들을 사용해 S3에 직접 영상과 썸네일을 업로드할 수 있습니다.
 * </p>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyVideoResponse {

    /**
     * 영상 업로드를 위한 Pre-signed URL
     */
    @JsonProperty("video_url")
    private String videoUrl;

    /**
     * 썸네일 업로드를 위한 Pre-signed URL
     */
    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;
    
    /**
     * 영상 파일의 S3 객체 키
     * <p>예: clips/456_20250508_191705.mp4</p>
     */
    @JsonProperty("video_key")
    private String videoKey;
    
    /**
     * 썸네일 파일의 S3 객체 키
     * <p>예: thumbnails/456_20250508_191705.jpg</p>
     */
    @JsonProperty("thumbnail_key")
    private String thumbnailKey;
} 