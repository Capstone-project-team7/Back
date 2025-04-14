package com.capstone.meerkatai.video.service;

import com.capstone.meerkatai.video.dto.GetVideoListResponse;
import com.capstone.meerkatai.video.entity.Video;
import com.capstone.meerkatai.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;

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
                        video.getAnomalyBehavior().getAnomalyBehaviorType().name() // 이 부분도 anomalyBehavior를 통해 접근해야 함
                ))
                .collect(Collectors.toList());

        // pagination 구성
        GetVideoListResponse.Pagination pagination = new GetVideoListResponse.Pagination(total, page, pages, limit);
        GetVideoListResponse.Data data = new GetVideoListResponse.Data(videoDtoList, pagination);

        return new GetVideoListResponse("success", data);
    }





    public List<Pair<String, InputStream>> getVideoStreams(Integer userId, List<Integer> videoIds) {
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



//    public List<Video> findAll() {
//        return videoRepository.findAll();
//    }
//
//    public Optional<Video> findById(Integer id) {
//        return videoRepository.findById(id);
//    }
//
//    public List<Video> findByUserId(Integer userId) {
//        return videoRepository.findByUserUserId(userId);
//    }
//
//    public List<Video> findByStreamingVideoId(Integer streamingVideoId) {
//        return videoRepository.findByStreamingVideoStreamingVideoId(streamingVideoId);
//    }
//
//    public Video save(Video video) {
//        return videoRepository.save(video);
//    }
//
//    public void delete(Integer id) {
//        videoRepository.deleteById(id);
//    }
}
