package com.capstone.meerkatai.streamingvideo.controller;

import com.capstone.meerkatai.streamingvideo.entity.StreamingVideo;
import com.capstone.meerkatai.streamingvideo.service.StreamingVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/streaming-videos")
@RequiredArgsConstructor
public class StreamingVideoController {

    private final StreamingVideoService streamingVideoService;
}
