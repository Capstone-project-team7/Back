package com.capstone.meerkatai.streamingvideo.repository;

import com.capstone.meerkatai.streamingvideo.entity.StreamingVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StreamingVideoRepository extends JpaRepository<StreamingVideo, Long> {
  List<StreamingVideo> findByUserUserId(Long userId);
  List<StreamingVideo> findByCctvCctvId(Long cctvId);

  Optional<StreamingVideo> findByUserUserIdAndCctvCctvId(Long userId, Long cctvId);
  List<StreamingVideo> findByUserUserIdAndStreamingVideoStatusTrue(Long cctvId);
}