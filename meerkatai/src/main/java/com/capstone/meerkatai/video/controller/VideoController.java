package com.capstone.meerkatai.video.controller;

import com.capstone.meerkatai.video.entity.Video;
import com.capstone.meerkatai.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @GetMapping
    public List<Video> getAll() {
        return videoService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Video> getById(@PathVariable Integer id) {
        return videoService.findById(id);
    }

    @GetMapping("/user/{userId}")
    public List<Video> getByUserId(@PathVariable Integer userId) {
        return videoService.findByUserId(userId);
    }

    @GetMapping("/streaming/{streamingVideoId}")
    public List<Video> getByStreamingVideoId(@PathVariable Integer streamingVideoId) {
        return videoService.findByStreamingVideoId(streamingVideoId);
    }

    @PostMapping
    public Video create(@RequestBody Video video) {
        return videoService.save(video);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        videoService.delete(id);
    }
}
