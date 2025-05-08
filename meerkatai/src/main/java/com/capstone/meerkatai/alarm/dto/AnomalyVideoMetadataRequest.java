package com.capstone.meerkatai.alarm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AnomalyVideoMetadataRequest {

    @JsonProperty("cctv_id")
    private Long cctvId;

    @JsonProperty("videoUrl")
    private String videoUrl;

    @JsonProperty("anomalyType")
    private String anomalyType;

    private Double confidence;

    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")  // ISO-8601 형식
    private LocalDateTime timestamp;

    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;

    @JsonProperty("user_id")
    private Long userId;
}
