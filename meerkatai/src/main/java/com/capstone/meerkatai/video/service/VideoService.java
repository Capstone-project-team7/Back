package com.capstone.meerkatai.video.service;

import com.capstone.meerkatai.video.entity.Video;
import com.capstone.meerkatai.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;

    public List<Video> findAll() {
        return videoRepository.findAll();
    }

    public Optional<Video> findById(Integer id) {
        return videoRepository.findById(id);
    }

    public List<Video> findByUserId(Integer userId) {
        return videoRepository.findByUserUserId(userId);
    }

    public List<Video> findByStreamingVideoId(Integer streamingVideoId) {
        return videoRepository.findByStreamingVideoStreamingVideoId(streamingVideoId);
    }

    public Video save(Video video) {
        return videoRepository.save(video);
    }

    public void delete(Integer id) {
        videoRepository.deleteById(id);
    }
}
