package com.capstone.meerkatai.video.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoFilterRequest {
    private String start_date;
    private String end_date;
    private String anomaly_type;
    private int page;
}

