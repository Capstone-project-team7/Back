package com.capstone.meerkatai.video.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VideoDetailsResponse {
    private Integer video_id;
    private String file_path;
    private String thumbnail_path;
    private Integer duration;
    private Integer file_size;
    private Boolean video_status;
    private String created_at;
    private Integer streaming_video_id;
    private Integer cctv_id;
    private String cctv_name;
    private Integer user_id;
    private String anomaly_type;
}

