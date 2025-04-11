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

    @GetMapping
    public List<StreamingVideo> getAll() {
        return streamingVideoService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<StreamingVideo> getById(@PathVariable Integer id) {
        return streamingVideoService.findById(id);
    }

    @GetMapping("/user/{userId}")
    public List<StreamingVideo> getByUserId(@PathVariable Integer userId) {
        return streamingVideoService.findByUserId(userId);
    }

    @GetMapping("/cctv/{cctvId}")
    public List<StreamingVideo> getByCctvId(@PathVariable Integer cctvId) {
        return streamingVideoService.findByCctvId(cctvId);
    }

    @PostMapping
    public StreamingVideo create(@RequestBody StreamingVideo streamingVideo) {
        return streamingVideoService.save(streamingVideo);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        streamingVideoService.delete(id);
    }
}
