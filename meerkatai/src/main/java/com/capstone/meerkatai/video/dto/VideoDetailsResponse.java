package com.capstone.meerkatai.video.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VideoDetailsResponse {
    private Long video_id;
    private String file_path;
    private String thumbnail_path;
    private Integer duration;
    private Integer file_size;
    private Boolean video_status;
    private String created_at;
    private Long streaming_video_id;
    private Long cctv_id;
    private String cctv_name;
    private Long user_id;
    private String anomaly_type;
}

