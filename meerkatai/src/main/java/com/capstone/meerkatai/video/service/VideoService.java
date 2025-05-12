package com.capstone.meerkatai.video.service;

import com.capstone.meerkatai.alarm.dto.AnomalyVideoMetadataRequest;
import com.capstone.meerkatai.anomalybehavior.entity.AnomalyBehavior;
import com.capstone.meerkatai.anomalybehavior.repository.AnomalyBehaviorRepository;
import com.capstone.meerkatai.global.service.S3Service;
import com.capstone.meerkatai.streamingvideo.entity.StreamingVideo;
import com.capstone.meerkatai.streamingvideo.repository.StreamingVideoRepository;
import com.capstone.meerkatai.user.entity.User;
import com.capstone.meerkatai.user.repository.UserRepository;
import com.capstone.meerkatai.video.dto.GetVideoListResponse;
import com.capstone.meerkatai.video.dto.VideoDetailsResponse;
import com.capstone.meerkatai.video.dto.VideoListRequest;
import com.capstone.meerkatai.video.entity.Video;
import com.capstone.meerkatai.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final StreamingVideoRepository streamingVideoRepository;
    private final AnomalyBehaviorRepository anomalyBehaviorRepository;
    private final S3Service s3Service;
    
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    //    필터 값 없는 경우(홈페이지 이동 OR 필터 값 없이 페이지 이동)
//    {
//        "start_date":,
//        "end_date":,
//        "anomaly_behavior_type":,
//        "page": 1
//    }
    public GetVideoListResponse getVideosByUser(Long userId, int page) {
        final int limit = 6;
        int offset = (page - 1) * limit;

        // 전체 영상 리스트 가져오기 (페이징 없는 상태)
        List<Video> allVideos = videoRepository.findByUserUserId(userId);
        int total = allVideos.size();
        int pages = (int) Math.ceil((double) total / limit);

        // Java로 페이징 처리
        List<Video> pagedVideos = allVideos.stream()
            .skip(offset)
            .limit(limit)
            .toList();

        // 엔티티 → DTO 변환
        List<GetVideoListResponse.VideoDto> videoDtoList = pagedVideos.stream()
            .map(video -> new GetVideoListResponse.VideoDto(
                video.getVideoId(),
                video.getFilePath(),
                video.getThumbnailPath(),
                video.getDuration(),
                video.getFileSize(),
                video.getVideoStatus(),
                video.getAnomalyBehavior().getAnomalyTime().toString(), // 수정된 부분
                video.getStreamingVideo().getStreamingVideoId(),
                video.getAnomalyBehavior().getAnomalyBehaviorType(), // 이 부분도 anomalyBehavior를 통해 접근해야 함
                video.getStreamingVideo().getCctv().getCctvName()
            ))
            .collect(Collectors.toList());

        // pagination 구성
        GetVideoListResponse.Pagination pagination = new GetVideoListResponse.Pagination(total, page, pages, limit);
        GetVideoListResponse.Data data = new GetVideoListResponse.Data(videoDtoList, pagination);

        return new GetVideoListResponse("success", data);
    }


    //    필터 값 있는 경우(날짜 선택 OR 유형 선택 OR 날짜, 유형 선택 OR )
    public GetVideoListResponse getVideosByFilters(Long userId, VideoListRequest req) {
        List<Video> allVideos = videoRepository.findByUserUserId(userId);
        Stream<Video> stream = allVideos.stream();

        // 날짜 필터 적용
        if (req.getStart_date() != null && !req.getStart_date().isBlank()
            && req.getEnd_date() != null && !req.getEnd_date().isBlank()) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(req.getStart_date(), formatter);
            LocalDate end = LocalDate.parse(req.getEnd_date(), formatter);

            stream = stream.filter(video -> {
                LocalDate date = video.getAnomalyBehavior().getAnomalyTime().toLocalDate();
                return !date.isBefore(start) && !date.isAfter(end);
            });
        }

        // 이상행동 유형 필터 적용
        if (req.getAnomaly_behavior_type() != null && !req.getAnomaly_behavior_type().isBlank()) {
            stream = stream.filter(video ->
                video.getAnomalyBehavior().getAnomalyBehaviorType().equalsIgnoreCase(req.getAnomaly_behavior_type())
            );
        }

        List<Video> filtered = stream.toList();

        // 페이지 값이 null이거나 1보다 작으면 기본값 1로 설정
        int page = (req.getPage() == null || req.getPage() < 1) ? 1 : req.getPage();
        int limit = 6;
        int total = filtered.size();
        int pages = (int) Math.ceil((double) total / limit);
        int offset = (page - 1) * limit;

        List<Video> pagedVideos = filtered.stream().skip(offset).limit(limit).toList();

        List<GetVideoListResponse.VideoDto> videoDtoList = pagedVideos.stream()
            .map(video -> new GetVideoListResponse.VideoDto(
                video.getVideoId(),
                video.getFilePath(),
                video.getThumbnailPath(),
                video.getDuration(),
                video.getFileSize(),
                video.getVideoStatus(),
                video.getAnomalyBehavior().getAnomalyTime().toString(),
                video.getStreamingVideo().getStreamingVideoId(),
                video.getAnomalyBehavior().getAnomalyBehaviorType(),
                video.getStreamingVideo().getCctv().getCctvName()
            ))
            .toList();

        GetVideoListResponse.Pagination pagination = new GetVideoListResponse.Pagination(total, page, pages, limit);
        return new GetVideoListResponse("success", new GetVideoListResponse.Data(videoDtoList, pagination));
    }





    // 비디오 다운로드 메소드
    public List<Pair<String, InputStream>> getVideoStreams(Long userId, List<Long> videoIds) {
        List<Video> videos = videoRepository.findByUser_UserIdAndVideoIdIn(userId, videoIds);

        List<Pair<String, InputStream>> result = new ArrayList<>();

        for (Video video : videos) {
            try {
                String path = video.getFilePath();
                InputStream stream;

                if (path.startsWith("http")) {
                    URL url = new URL(path);
                    stream = url.openStream();
                } else if (path.startsWith("s3://")) {
                    // S3 경로 형식: s3://버킷명/객체키
                    String objectKey = path.substring(("s3://" + bucketName + "/").length());
                    URL presignedUrl = s3Service.generatePresignedUrlForDownload(objectKey);
                    stream = presignedUrl.openStream();
                } else {
                    File file = new File(path);
                    if (!file.exists()) continue;
                    stream = new FileInputStream(file);
                }

                result.add(Pair.of("video_" + video.getVideoId() + ".mp4", stream));
            } catch (Exception e) {
                log.error("비디오 스트림 가져오기 실패: {} ({})", video.getVideoId(), e.getMessage());
            }
        }

        return result;
    }

    //비디오 삭제 메소드
    @Transactional
    public List<Long> deleteVideosByUser(Long userId, List<Long> videoIds) {
        // 1. userId와 videoIds로 사용자 본인의 영상만 필터링
        List<Video> videos = videoRepository.findByUser_UserIdAndVideoIdIn(userId, videoIds);

        // 2. S3에서 영상 및 썸네일 파일 삭제
        for (Video video : videos) {
            try {
                String filePath = video.getFilePath();
                String thumbnailPath = video.getThumbnailPath();
                
                // S3 경로인 경우에만 삭제 시도
                if (filePath != null && filePath.startsWith("s3://")) {
                    String objectKey = filePath.substring(("s3://" + bucketName + "/").length());
                    s3Service.deleteObject(objectKey);
                }
                
                if (thumbnailPath != null && thumbnailPath.startsWith("s3://")) {
                    String objectKey = thumbnailPath.substring(("s3://" + bucketName + "/").length());
                    s3Service.deleteObject(objectKey);
                }
            } catch (Exception e) {
                log.error("S3 파일 삭제 실패: {} ({})", video.getVideoId(), e.getMessage());
            }
        }

        // 3. DB에서 삭제
        videoRepository.deleteAll(videos);

        // 4. 실제 삭제된 ID만 반환
        return videos.stream()
            .map(Video::getVideoId)
            .collect(Collectors.toList());
    }

    // 비디오 세부 내용 조회 메소드
    public VideoDetailsResponse getVideoDetails(Long userId, Long videoId) {
        Video video = videoRepository.findByUserUserIdAndVideoId(userId, videoId)
            .orElseThrow(() -> new RuntimeException("비디오 없음"));

        return new VideoDetailsResponse(
            video.getVideoId(),
            video.getFilePath(),
            video.getThumbnailPath(),
            video.getDuration(),
            video.getFileSize(),
            video.getVideoStatus(),
            video.getAnomalyBehavior().getAnomalyTime().toString(),
            video.getStreamingVideo().getStreamingVideoId(),
            video.getStreamingVideo().getCctv().getCctvId(),
            video.getStreamingVideo().getCctv().getCctvName(),
            video.getUser().getUserId(),
            video.getAnomalyBehavior().getAnomalyBehaviorType()
        );
    }


    @Transactional
    public Video saveVideo(AnomalyVideoMetadataRequest request, AnomalyBehavior anomalyBehavior) {
        // 1. 연관 엔티티 조회
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        StreamingVideo streamingVideo = streamingVideoRepository.findById(request.getCctvId())
                .orElseThrow(() -> new RuntimeException("스트리밍 비디오 없음"));

        // 2. S3 객체 키 생성
        String videoKey = s3Service.generateVideoKey(request.getCctvId());
        String thumbnailKey = s3Service.generateThumbnailKey(videoKey);
        
        // 3. S3 URL 생성
        URL videoPresignedUrl = s3Service.generatePresignedUrlForUpload(videoKey);
        URL thumbnailPresignedUrl = s3Service.generatePresignedUrlForUpload(thumbnailKey);
        
        // 4. S3 경로 생성 (s3://버킷명/객체키 형식)
        String videoFullPath = "s3://" + bucketName + "/" + videoKey;
        String thumbnailFullPath = "s3://" + bucketName + "/" + thumbnailKey;
        
        // 5. 영상 정보 분석 (가능하다면, 업로드 전이라 실제 데이터가 없으면 스킵)
        long fileSize = getRemoteFileSize(request.getVideoUrl());
        double duration = 0;
        boolean playable = true; // 기본적으로 재생 가능으로 설정
        
        if (request.getVideoUrl() != null && !request.getVideoUrl().isEmpty()) {
            try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(new URL(request.getVideoUrl()))) {
                grabber.start();
                duration = grabber.getLengthInTime() / 1_000_000.0; // 초 단위
                playable = grabber.getLengthInFrames() > 0;
                grabber.stop();
            } catch (Exception e) {
                log.warn("⚠️ 영상 분석 실패: {}", e.getMessage());
            }
        }

        // 6. 비디오 엔티티 저장
        Video video = Video.builder()
                .filePath(videoFullPath)
                .thumbnailPath(thumbnailFullPath)
                .duration((long)duration)
                .fileSize(fileSize)
                .videoStatus(playable)
                .streamingVideo(streamingVideo)
                .anomalyBehavior(anomalyBehavior)
                .user(user)
                .build();

        Video saved = videoRepository.save(video);
        log.info("✅ 비디오 저장 완료: video_id={}, 영상 경로={}, 썸네일 경로={}", 
                saved.getVideoId(), videoFullPath, thumbnailFullPath);
                
        // 7. 실제 Upload URL 로그 남기기 (클라이언트에서 업로드에 사용)
        log.info("✅ 영상 업로드 URL: {}", videoPresignedUrl);
        log.info("✅ 썸네일 업로드 URL: {}", thumbnailPresignedUrl);
                
        return saved;
    }

    private long getRemoteFileSize(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("HEAD");
            conn.getInputStream();
            return conn.getContentLengthLong(); // ✅ 파일 크기 (byte)
        } catch (Exception e) {
            System.err.println("파일 크기 확인 실패: " + e.getMessage());
            return 0;
        }
    }

}