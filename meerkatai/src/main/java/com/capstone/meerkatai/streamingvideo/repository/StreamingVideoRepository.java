package com.capstone.meerkatai.streamingvideo.repository;

import com.capstone.meerkatai.streamingvideo.entity.StreamingVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StreamingVideoRepository extends JpaRepository<StreamingVideo, Long> {
    List<StreamingVideo> findByUserUserId(Integer userId);
    List<StreamingVideo> findByCctvCctvId(Integer cctvId);
}
