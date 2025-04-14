package com.capstone.meerkatai.video.controller;

import com.capstone.meerkatai.video.dto.GetVideoListResponse;
import com.capstone.meerkatai.video.dto.VideoDeleteRequest;
import com.capstone.meerkatai.video.dto.VideoDownloadRequest;
import com.capstone.meerkatai.video.entity.Video;
import com.capstone.meerkatai.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/v1/video")
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


    //JWT 작성 전 임시 코드
    @PostMapping("/download")
    public ResponseEntity<?> downloadVideos(
            @RequestParam("userId") Integer userId,
            @RequestBody VideoDownloadRequest request
    ) {
        try {
            List<Pair<String, InputStream>> files = videoService.getVideoStreams(userId, request.getVideoIds());

            if (files.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "error", "message", "비디오를 찾을 수 없거나 다운로드할 수 없습니다."));
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);

            for (Pair<String, InputStream> pair : files) {
                zos.putNextEntry(new ZipEntry(pair.getFirst()));
                StreamUtils.copy(pair.getSecond(), zos);
                pair.getSecond().close();
                zos.closeEntry();
            }

            zos.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment().filename("videos.zip").build());

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(baos.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "비디오를 찾을 수 없거나 다운로드할 수 없습니다."));
        }
    }

    //JWT 토큰 버전 다운로드 api
//    @PostMapping("/download")
//    public ResponseEntity<?> downloadVideos(
//            @RequestHeader("Authorization") String authHeader,
//            @RequestBody VideoDownloadRequest request
//    ) {
//        try {
//            // 1. JWT 토큰에서 userId 추출
//            String token = authHeader.replace("Bearer ", "");
//            Integer userId = jwtUtil.getUserIdFromToken(token).intValue();  // Long → Integer
//
//            // 2. 영상 조회
//            List<Pair<String, InputStream>> files = videoService.getVideoStreams(userId, request.getVideoIds());
//
//            if (files.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body(Map.of("status", "error", "message", "비디오를 찾을 수 없거나 다운로드할 수 없습니다."));
//            }
//
//            // 3. Zip 압축 처리
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            ZipOutputStream zos = new ZipOutputStream(baos);
//
//            for (Pair<String, InputStream> pair : files) {
//                zos.putNextEntry(new ZipEntry(pair.getFirst()));
//                StreamUtils.copy(pair.getSecond(), zos);
//                pair.getSecond().close();
//                zos.closeEntry();
//            }
//
//            zos.close();
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//            headers.setContentDisposition(ContentDisposition.attachment().filename("videos.zip").build());
//
//            return ResponseEntity.ok()
//                    .headers(headers)
//                    .body(baos.toByteArray());
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("status", "error", "message", "비디오를 찾을 수 없거나 다운로드할 수 없습니다."));
//        }
//    }

    //JWT 작성 전 임시 코드
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteVideos(
            @RequestParam("userId") Integer userId,
            @RequestBody VideoDeleteRequest request
    ) {
        try {
            // 비디오 삭제 요청 처리
            List<Integer> deletedIds = videoService.deleteVideosByUser(userId, request.getVideoIds());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", Map.of(
                            "deletedIds", deletedIds
                    )
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "영상 삭제 중 오류가 발생했습니다."
            ));
        }
    }


    //JWT 토큰 버전 delete api
//    @DeleteMapping("/delete")
//    public ResponseEntity<?> deleteVideos(
//            @RequestHeader("Authorization") String authHeader,
//            @RequestBody VideoDeleteRequest request
//    ) {
//        try {
//            // 1. JWT 토큰에서 userId 추출
//            String token = authHeader.replace("Bearer ", "");
//            Integer userId = jwtUtil.getUserIdFromToken(token).intValue();
//
//            // 2. 비디오 삭제 요청 처리
//            List<Integer> deletedIds = videoService.deleteVideosByUser(userId, request.getVideoIds());
//
//            return ResponseEntity.ok(Map.of(
//                    "status", "success",
//                    "data", Map.of(
//                            "deletedIds", deletedIds
//                    )
//            ));
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                    "status", "error",
//                    "message", "영상 삭제 중 오류가 발생했습니다."
//            ));
//        }
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
