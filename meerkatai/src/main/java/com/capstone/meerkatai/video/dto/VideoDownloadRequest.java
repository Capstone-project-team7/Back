package com.capstone.meerkatai.video.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class VideoDownloadRequest {
    private List<Long> videoIds;
}
