package com.capstone.meerkatai.video.controller;

import com.capstone.meerkatai.video.dto.GetVideoListResponse;
import com.capstone.meerkatai.video.entity.Video;
import com.capstone.meerkatai.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    //JWT 작성 전 임시 코드
    // 예시: /api/v1/video/list/3?page=1
    @GetMapping("/list/{userId}")
    public ResponseEntity<GetVideoListResponse> getVideosByUser(
            @PathVariable("userId") Integer userId,
            @RequestParam(value = "page", defaultValue = "1") int page
    ) {
        GetVideoListResponse response = videoService.getVideosByUser(userId, page);
        return ResponseEntity.ok(response);
    }

    // JWT 코드 작성 후 이걸로 하면 될듯.
//    @GetMapping("/list")
//    public ResponseEntity<GetVideoListResponse> getVideosByUser(
//            @RequestHeader("Authorization") String authHeader,
//            @RequestParam(value = "page", defaultValue = "1") int page
//    ) {
//        String token = authHeader.replace("Bearer ", "");
//        Integer userId = jwtUtil.getUserIdFromToken(token).intValue(); // userId가 Long이면 int로 변환
//
//        GetVideoListResponse response = videoService.getVideosByUser(userId, page);
//        return ResponseEntity.ok(response);
//    }


//    @GetMapping
//    public List<Video> getAll() {
//        return videoService.findAll();
//    }
//
//    @GetMapping("/{id}")
//    public Optional<Video> getById(@PathVariable Integer id) {
//        return videoService.findById(id);
//    }
//
//    @GetMapping("/user/{userId}")
//    public List<Video> getByUserId(@PathVariable Integer userId) {
//        return videoService.findByUserId(userId);
//    }
//
//    @GetMapping("/streaming/{streamingVideoId}")
//    public List<Video> getByStreamingVideoId(@PathVariable Integer streamingVideoId) {
//        return videoService.findByStreamingVideoId(streamingVideoId);
//    }
//
//    @PostMapping
//    public Video create(@RequestBody Video video) {
//        return videoService.save(video);
//    }
//
//    @DeleteMapping("/{id}")
//    public void delete(@PathVariable Integer id) {
//        videoService.delete(id);
//    }
}
