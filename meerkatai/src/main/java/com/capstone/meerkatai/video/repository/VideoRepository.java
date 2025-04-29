package com.capstone.meerkatai.video.repository;

import com.capstone.meerkatai.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByUserUserId(Long userId);
    List<Video> findByStreamingVideoStreamingVideoId(Long streamingVideoId);
    List<Video> findByUser_UserIdAndVideoIdIn(Long userId, List<Long> videoIds);
    Optional<Video> findByUserUserIdAndVideoId(Long userId, Long videoId);
}