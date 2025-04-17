package com.capstone.meerkatai.video.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoListRequest {
  private String start_date;
  private String end_date;
  private String anomaly_behavior_type;
  private Integer page;
}