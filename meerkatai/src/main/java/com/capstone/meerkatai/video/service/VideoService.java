package com.capstone.meerkatai.video.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
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
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            .map(video -> {
                // S3 URL을 presigned URL로 변환
                String videoPath = generatePresignedUrlIfNeeded(video.getFilePath());
                String thumbnailPath = generatePresignedUrlIfNeeded(video.getThumbnailPath());
                
                return new GetVideoListResponse.VideoDto(
                    video.getVideoId(),
                    videoPath,
                    thumbnailPath,
                    video.getDuration(),
                    video.getFileSize(),
                    video.getVideoStatus(),
                    video.getAnomalyBehavior().getAnomalyTime().toString(),
                    video.getStreamingVideo().getStreamingVideoId(),
                    video.getAnomalyBehavior().getAnomalyBehaviorType(),
                    video.getStreamingVideo().getCctv().getCctvName()
                );
            })
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

//        // 이상행동 유형 필터 적용
//        if (req.getAnomaly_behavior_type() != null && !req.getAnomaly_behavior_type().isBlank()) {
//            stream = stream.filter(video ->
//                video.getAnomalyBehavior().getAnomalyBehaviorType().equalsIgnoreCase(req.getAnomaly_behavior_type())
//            );
//        }

        // 이상행동 유형 필터 적용
        if (req.getAnomaly_behavior_type() != null && !req.getAnomaly_behavior_type().isBlank()) {
            String typeKey = req.getAnomaly_behavior_type().toLowerCase();

            // typeX -> 키워드 매핑
            Map<String, String> keywordMap = Map.of(
                    "type1", "전도",
                    "type2", "파손",
                    "type3", "방화",
                    "type4", "흡연",
                    "type5", "유기",
                    "type6", "절도",
                    "type7", "폭행"
            );

            String targetKeyword = keywordMap.get(typeKey);

            if (targetKeyword != null) {
                stream = stream.filter(video ->
                        video.getAnomalyBehavior() != null &&
                                video.getAnomalyBehavior().getAnomalyBehaviorType() != null &&
                                video.getAnomalyBehavior().getAnomalyBehaviorType().contains(targetKeyword)
                );
            }
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
            .map(video -> {
                // S3 URL을 presigned URL로 변환
                String videoPath = generatePresignedUrlIfNeeded(video.getFilePath());
                String thumbnailPath = generatePresignedUrlIfNeeded(video.getThumbnailPath());
                
                return new GetVideoListResponse.VideoDto(
                    video.getVideoId(),
                    videoPath,
                    thumbnailPath,
                    video.getDuration(),
                    video.getFileSize(),
                    video.getVideoStatus(),
                    video.getAnomalyBehavior().getAnomalyTime().toString(),
                    video.getStreamingVideo().getStreamingVideoId(),
                    video.getAnomalyBehavior().getAnomalyBehaviorType(),
                    video.getStreamingVideo().getCctv().getCctvName()
                );
            })
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
                    // S3 URL인 경우 Presigned URL 생성하여 처리
                    if (s3Service.isS3Url(path)) {
                        // 객체 키 추출 (S3 URL에서 버킷 이름 이후 부분)
                        String objectKey = s3Service.extractS3Key(path);
                        URL presignedUrl = s3Service.generatePresignedUrlForDownload(objectKey);
                        stream = presignedUrl.openStream();
                    } else {
                        // 일반 HTTP URL
                        URL url = new URL(path);
                        stream = url.openStream();
                    }
                } else {
                    File file = new File(path);
                    if (!file.exists()) continue;
                    stream = new FileInputStream(file);
                }

                result.add(Pair.of("video_" + video.getVideoId() + ".mp4", stream));
            } catch (Exception e) {
                log.error("비디오 스트림 생성 중 오류 발생: {}", e.getMessage());
            }
        }

        return result;
    }

    //비디오 삭제 메소드
    public List<Long> deleteVideosByUser(Long userId, List<Long> videoIds) {
        // 1. userId와 videoIds로 사용자 본인의 영상만 필터링
        List<Video> videos = videoRepository.findByUser_UserIdAndVideoIdIn(userId, videoIds);

        // S3에서 객체 삭제 처리 추가
        for (Video video : videos) {
            try {
                // 비디오 파일 삭제
                if (s3Service.isS3Url(video.getFilePath())) {
                    String videoKey = s3Service.extractS3Key(video.getFilePath());
                    s3Service.deleteObject(videoKey);
                    log.info("S3에서 비디오 삭제 완료: {}", videoKey);
                }
                
                // 썸네일 삭제
                if (s3Service.isS3Url(video.getThumbnailPath())) {
                    String thumbnailKey = s3Service.extractS3Key(video.getThumbnailPath());
                    s3Service.deleteObject(thumbnailKey);
                    log.info("S3에서 썸네일 삭제 완료: {}", thumbnailKey);
                }
            } catch (Exception e) {
                log.error("S3 객체 삭제 중 오류: {}", e.getMessage());
            }
        }

        // 2. DB에서 삭제
        videoRepository.deleteAll(videos);

        // 3. 실제 삭제된 ID만 반환
        return videos.stream()
            .map(Video::getVideoId)
            .collect(Collectors.toList());
    }

    // 비디오 세부 내용 조회 메소드
    public VideoDetailsResponse getVideoDetails(Long userId, Long videoId) {
        Video video = videoRepository.findByUserUserIdAndVideoId(userId, videoId)
            .orElseThrow(() -> new RuntimeException("비디오 없음"));

        // S3 URL을 presigned URL로 변환
        String videoPath = generatePresignedUrlIfNeeded(video.getFilePath());
        String thumbnailPath = generatePresignedUrlIfNeeded(video.getThumbnailPath());

        return new VideoDetailsResponse(
            video.getVideoId(),
            videoPath,
            thumbnailPath,
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


    public Video saveVideo(AnomalyVideoMetadataRequest request, AnomalyBehavior anomalyBehavior) {
        // 1. 연관 엔티티 조회
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // StreamingVideo를 CCTV ID로 조회 (기존 방식 수정)
        List<StreamingVideo> streamingVideos = streamingVideoRepository.findByCctvCctvId(request.getCctvId());
        if (streamingVideos.isEmpty()) {
            throw new RuntimeException("스트리밍 비디오 없음: CCTV ID=" + request.getCctvId());
        }
        StreamingVideo streamingVideo = streamingVideos.get(0);
        log.info("StreamingVideo 조회 성공: id={}", streamingVideo.getStreamingVideoId());

        // 2. 영상 정보 분석
        String videoUrl = request.getVideoUrl();
        String thumbnailUrl = request.getThumbnailUrl();
        
        // S3 URL 검증 및 처리
        if (!s3Service.isS3Url(videoUrl)) {
            log.warn("비디오 URL이 S3 URL 형식이 아닙니다: {}", videoUrl);
        }
        
        // 썸네일 URL 처리
        if (thumbnailUrl == null || thumbnailUrl.trim().isEmpty()) {
            // S3 URL 패턴에 맞게 썸네일 URL 생성
            if (videoUrl != null && !videoUrl.trim().isEmpty()) {
                thumbnailUrl = s3Service.generateThumbnailUrlFromVideoUrl(videoUrl);
                log.info("비디오 URL에서 썸네일 URL 생성: {}", thumbnailUrl);
            } else {
                // 기본 썸네일 URL 설정
                thumbnailUrl = "https://cctv-recordings-yuhan-20250505.s3.ap-northeast-2.amazonaws.com/thumbnails/default.jpg";
                log.warn("썸네일 URL 생성 불가, 기본값 사용: {}", thumbnailUrl);
            }
        }
        
        // 3. S3에서 직접 메타데이터 가져오기
        String videoKey = null;
        long fileSize = 0;
        double duration = 0;
        boolean playable = false;
        Map<String, String> userMetadata = null;

        try {
            // S3 객체 키 추출
            videoKey = s3Service.extractS3Key(videoUrl);
            log.info("추출된 S3 객체 키: {}", videoKey);
            
            // S3에서 메타데이터 가져오기
            ObjectMetadata metadata = s3Service.getObjectMetadata(videoKey);
            if (metadata != null) {
                // 파일 크기
                fileSize = metadata.getContentLength();
                log.info("S3 메타데이터에서 파일 크기 조회: {} 바이트", fileSize);
                
                // 사용자 정의 메타데이터
                userMetadata = metadata.getUserMetadata();
                
                // 메타데이터에서 재생 시간 정보 가져오기 (있는 경우)
                if (userMetadata != null && userMetadata.containsKey("video-duration")) {
                    try {
                        duration = Double.parseDouble(userMetadata.get("video-duration"));
                        playable = true;
                        log.info("S3 메타데이터에서 영상 길이 조회: {}초", duration);
                    } catch (NumberFormatException e) {
                        log.warn("메타데이터에서 영상 길이를 파싱할 수 없습니다. 기본값 사용");
                    }
                }
            } else {
                log.warn("S3 메타데이터를 가져올 수 없습니다. 대체 방법 시도");
            }
            
            // 메타데이터에서 재생 시간을 가져오지 못한 경우, FFmpeg로 분석 시도
            if (duration <= 0) {
                log.info("FFmpeg로 영상 분석 시도");
                analyzeVideoWithFFmpeg(videoUrl, videoKey);
            }
        } catch (Exception e) {
            log.error("S3 메타데이터 조회 중 오류 발생: {}", e.getMessage());
        }

        // 4. 비디오 엔티티 저장
        Video video = Video.builder()
                .filePath(videoUrl)
                .thumbnailPath(thumbnailUrl)
                .duration((long)duration)
                .fileSize(fileSize)
                .videoStatus(playable)
                .streamingVideo(streamingVideo)
                .anomalyBehavior(anomalyBehavior)
                .user(user)
                .build();

        Video saved = videoRepository.save(video);
        log.info("✅ 비디오 저장 완료: video_id={}", saved.getVideoId());
        return saved;
    }
    
    /**
     * FFmpeg를 사용하여 비디오 분석
     * 
     * @param videoUrl 비디오 URL
     * @param objectKey S3 객체 키
     * @return [재생 시간, 재생 가능 여부] 배열
     */
    private double[] analyzeVideoWithFFmpeg(String videoUrl, String objectKey) {
        double duration = 0;
        boolean playable = false;
        
        try {
            // Presigned URL 생성
            URL presignedUrl = s3Service.generatePresignedUrlForDownload(objectKey);
            
            try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(presignedUrl.toString())) {
                grabber.start();
                duration = grabber.getLengthInTime() / 1_000_000.0; // 초 단위
                playable = grabber.getLengthInFrames() > 0;
                grabber.stop();
                
                log.info("FFmpeg로 비디오 분석 성공: duration={}초, playable={}", duration, playable);
            } catch (Exception e) {
                log.error("FFmpeg 비디오 분석 실패: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("Presigned URL 생성 중 오류: {}", e.getMessage());
        }
        
        return new double[]{duration, playable ? 1.0 : 0.0};
    }
    
    /**
     * S3 URL인 경우 Presigned URL로 변환, 아닌 경우 원래 URL 반환
     */
    private String generatePresignedUrlIfNeeded(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        
        try {
            if (s3Service.isS3Url(url)) {
                String objectKey = s3Service.extractS3Key(url);
                if (objectKey != null) {
                    URL presignedUrl = s3Service.generatePresignedUrlForDownload(objectKey);
                    return presignedUrl.toString();
                }
            }
        } catch (Exception e) {
            log.warn("Presigned URL 생성 실패, 원본 URL 사용: {}", e.getMessage());
        }
        
        return url;
    }
    
    /**
     * 원격 파일 크기 조회 (S3 URL 처리 포함)
     */
    private long getRemoteFileSize(String url) {
        try {
            // S3 URL인 경우 S3 API로 파일 크기 가져오기
            if (s3Service.isS3Url(url)) {
                String objectKey = s3Service.extractS3Key(url);
                
                // S3 메타데이터에서 파일 크기 조회
                long size = s3Service.getObjectSize(objectKey);
                if (size > 0) {
                    log.info("S3 메타데이터에서 파일 크기 조회: {} 바이트", size);
                    return size;
                }
                
                // S3 메타데이터로 조회 실패한 경우 Presigned URL로 시도
                log.info("Presigned URL로 파일 크기 조회 시도");
                URL presignedUrl = s3Service.generatePresignedUrlForDownload(objectKey);
                
                try {
                    HttpURLConnection conn = (HttpURLConnection) presignedUrl.openConnection();
                    conn.setRequestMethod("HEAD");
                    conn.getInputStream();
                    return conn.getContentLengthLong();
                } catch (Exception e) {
                    log.error("Presigned URL로 파일 크기 조회 실패: {}", e.getMessage());
                }
                return 0;
            } else {
                // 일반 HTTP URL
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("HEAD");
                conn.getInputStream();
                return conn.getContentLengthLong();
            }
        } catch (Exception e) {
            log.error("파일 크기 확인 실패: {}", e.getMessage());
            return 0;
        }
    }
}