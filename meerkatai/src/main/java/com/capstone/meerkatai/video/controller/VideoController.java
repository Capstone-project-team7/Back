package com.capstone.meerkatai.video.controller;

import com.capstone.meerkatai.user.repository.UserRepository;
import com.capstone.meerkatai.video.dto.*;
import com.capstone.meerkatai.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/v1/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final UserRepository userRepository;

    // ✅ 공통 메서드: 현재 사용자 ID 조회
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."))
                .getUserId(); // User 엔티티에서 실제 ID 필드명에 맞게 수정
    }


    // 📄 영상 리스트 조회.
    // POST: http://localhost:8080/api/v1/video/list
    //    {
    //        "start_date":"2024-01-01",
    //        "end_date":"2024-12-31",
    //        "anomaly_behavior_type":"Type1",
    //        "page": 1
    //    }
    @PostMapping("/list")
    public ResponseEntity<GetVideoListResponse> getVideosByUser(
            @RequestBody VideoListRequest request
    ) {
        Long userId = getCurrentUserId();

        // ✅ 페이지 기본값 처리
        int page = Optional.ofNullable(request.getPage()).orElse(1);
        if (page < 1) page = 1;

        // 필터 파라미터 존재 여부 확인
        boolean hasFilters = (request.getStart_date() != null && !request.getStart_date().isBlank()) ||
                (request.getEnd_date() != null && !request.getEnd_date().isBlank()) ||
                (request.getAnomaly_behavior_type() != null && !request.getAnomaly_behavior_type().isBlank());

        GetVideoListResponse response;
        if (hasFilters) {
            response = videoService.getVideosByFilters(userId, request);  // 필터 기반 조회 -> 필터 값 있는 경우
        } else {
            response = videoService.getVideosByUser(userId, request.getPage());  // 전체 조회 -> 홈페이지 이동 OR 필터값 없이 페이지 이동
        }

        return ResponseEntity.ok(response);
    }


    // 📥 영상 다운로드
    // POST: http://localhost:8080/api/v1/video/download
    //    {
    //        "videoIds": [201, 202]
    //    }
    @PostMapping("/download")
    public ResponseEntity<?> downloadVideos(
            @RequestBody VideoDownloadRequest request
    ) {
        try {
            Long userId = getCurrentUserId();

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

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(baos.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "비디오를 찾을 수 없거나 다운로드할 수 없습니다."));
        }
    }

    // 🗑️ 영상 삭제
    // DELETE: http://localhost:8080/api/v1/video/download
    //    {
    //        "videoIds": [201, 202]
    //    }
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteVideos(
            @RequestBody VideoDeleteRequest request
    ) {
        try {
            Long userId = getCurrentUserId();

            List<Long> deletedIds = videoService.deleteVideosByUser(userId, request.getVideoIds());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", Map.of("deletedIds", deletedIds)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "영상 삭제 중 오류가 발생했습니다."
            ));
        }
    }

    // 🔍 영상 상세 보기
    // GET: http://localhost:8080/api/v1/video/view/202
    @GetMapping("/view/{videoId}")
    public ResponseEntity<?> getVideoDetails(@PathVariable Long videoId) {
        try {
            Long userId = getCurrentUserId();
            VideoDetailsResponse response = videoService.getVideoDetails(userId, videoId);

            return ResponseEntity.ok(Map.of("status", "success", "data", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", "error",
                    "message", "비디오 정보를 찾을 수 없습니다."
            ));
        }
    }
}