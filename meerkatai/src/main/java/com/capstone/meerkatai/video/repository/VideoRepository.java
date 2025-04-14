package com.capstone.meerkatai.video.repository;

import com.capstone.meerkatai.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Integer> {
    List<Video> findByUserUserId(Integer userId);
    List<Video> findByStreamingVideoStreamingVideoId(Integer streamingVideoId);
    List<Video> findByUser_UserIdAndVideoIdIn(Integer userId, List<Integer> videoIds);
}
