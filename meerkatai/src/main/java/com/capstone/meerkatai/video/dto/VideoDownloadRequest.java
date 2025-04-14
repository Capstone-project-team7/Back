package com.capstone.meerkatai.video.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class VideoDownloadRequest {
    private Integer userId;
    private List<Integer> videoIds;
}
