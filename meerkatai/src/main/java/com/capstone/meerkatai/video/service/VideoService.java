package com.capstone.meerkatai.video.service;

import com.capstone.meerkatai.video.dto.GetVideoListResponse;
import com.capstone.meerkatai.video.dto.VideoDetailsResponse;
import com.capstone.meerkatai.video.dto.VideoListRequest;
import com.capstone.meerkatai.video.entity.Video;
import com.capstone.meerkatai.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;


//    필터 값 없는 경우(홈페이지 이동 OR 필터 값 없이 페이지 이동)
//    {
//        "start_date":,
//        "end_date":,
//        "anomaly_behavior_type":,
//        "page": 1
//    }
    public GetVideoListResponse getVideosByUser(Integer userId, int page) {
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
                        video.getVideoId().longValue(),
                        video.getFilePath(),
                        video.getThumbnailPath(),
                        video.getDuration(),
                        video.getFileSize(),
                        video.getVideoStatus(),
                        video.getAnomalyBehavior().getAnomalyTime().toString(), // 수정된 부분
                        video.getStreamingVideo().getStreamingVideoId().longValue(),
                        video.getAnomalyBehavior().getAnomalyBehaviorType().name(), // 이 부분도 anomalyBehavior를 통해 접근해야 함
                        video.getStreamingVideo().getCctv().getCctvName()
                ))
                .collect(Collectors.toList());

        // pagination 구성
        GetVideoListResponse.Pagination pagination = new GetVideoListResponse.Pagination(total, page, pages, limit);
        GetVideoListResponse.Data data = new GetVideoListResponse.Data(videoDtoList, pagination);

        return new GetVideoListResponse("success", data);
    }


//    필터 값 있는 경우(날짜 선택 OR 유형 선택 OR 날짜, 유형 선택 OR )
public GetVideoListResponse getVideosByFilters(Integer userId, VideoListRequest req) {
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
                video.getAnomalyBehavior().getAnomalyBehaviorType().name().equalsIgnoreCase(req.getAnomaly_behavior_type())
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
                    video.getVideoId().longValue(),
                    video.getFilePath(),
                    video.getThumbnailPath(),
                    video.getDuration(),
                    video.getFileSize(),
                    video.getVideoStatus(),
                    video.getAnomalyBehavior().getAnomalyTime().toString(),
                    video.getStreamingVideo().getStreamingVideoId().longValue(),
                    video.getAnomalyBehavior().getAnomalyBehaviorType().name(),
                    video.getStreamingVideo().getCctv().getCctvName()
            ))
            .toList();

    GetVideoListResponse.Pagination pagination = new GetVideoListResponse.Pagination(total, page, pages, limit);
    return new GetVideoListResponse("success", new GetVideoListResponse.Data(videoDtoList, pagination));
}





    // 비디오 다운로드 메소드
    public List<Pair<String, InputStream>> getVideoStreams(Integer userId, List<Long> videoIds) {
        List<Video> videos = videoRepository.findByUser_UserIdAndVideoIdIn(userId, videoIds);

        List<Pair<String, InputStream>> result = new ArrayList<>();

        for (Video video : videos) {
            try {
                String path = video.getFilePath();
                InputStream stream;

                if (path.startsWith("http")) {
                    URL url = new URL(path);
                    stream = url.openStream();
                } else {
                    File file = new File(path);
                    if (!file.exists()) continue;
                    stream = new FileInputStream(file);
                }

                result.add(Pair.of("video_" + video.getVideoId() + ".mp4", stream));
            } catch (Exception ignored) {}
        }

        return result;
    }

    //비디오 삭제 메소드
    public List<Long> deleteVideosByUser(Integer userId, List<Long> videoIds) {
        // 1. userId와 videoIds로 사용자 본인의 영상만 필터링
        List<Video> videos = videoRepository.findByUser_UserIdAndVideoIdIn(userId, videoIds);

        // 2. 삭제
        videoRepository.deleteAll(videos);

        // 3. 실제 삭제된 ID만 반환
        return videos.stream()
                .map(Video::getVideoId)
                .collect(Collectors.toList());
    }

    // 비디오 세부 내용 조회 메소드
    public VideoDetailsResponse getVideoDetails(Integer userId, Long videoId) {
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
                video.getAnomalyBehavior().getAnomalyBehaviorType().name()
        );
    }
}
