package com.capstone.meerkatai.video.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetVideoListResponse {
    private String status;
    private Data data;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private List<VideoDto> videos;
        private Pagination pagination;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoDto {
        private Long video_id;
        private String file_path;
        private String thumbnail_path;
        private int duration;
        private long file_size;
        private boolean video_status;
        private String created_at;
        private Long streaming_video_id;
        private String anomaly_behavior_type;
        private String cctv_name;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pagination {
        private int total;
        private int page;
        private int pages;
        private int limit;
    }
}