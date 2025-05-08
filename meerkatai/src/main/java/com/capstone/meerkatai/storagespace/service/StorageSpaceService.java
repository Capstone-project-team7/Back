package com.capstone.meerkatai.storagespace.service;

import com.capstone.meerkatai.alarm.dto.AnomalyVideoMetadataRequest;
import com.capstone.meerkatai.storagespace.entity.StorageSpace;
import com.capstone.meerkatai.storagespace.repository.StorageSpaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageSpaceService {

    private final StorageSpaceRepository storageSpaceRepository;

    public void updateUsedSpace(AnomalyVideoMetadataRequest request) {
        Long userId = request.getUserId();

        // 1. 사용자에 해당하는 저장공간 엔티티 조회
        StorageSpace storageSpace = storageSpaceRepository.findByUserUserId(userId)
                .orElseThrow(() -> new RuntimeException("저장공간 정보 없음"));

        // 2. 영상 URL로부터 파일 사이즈 추출
        long fileSizeInBytes = getRemoteFileSize(request.getVideoUrl());

        // 3. 기존 used_space에 더해서 갱신
        long currentUsed = storageSpace.getUsedSpace() != null ? storageSpace.getUsedSpace() : 0;
        storageSpace.setUsedSpace(currentUsed + fileSizeInBytes);

        // 4. 저장
        storageSpaceRepository.save(storageSpace);
        log.info("✅ 저장공간 갱신 완료: user={}, 추가 사용량={} byte", userId, fileSizeInBytes);
    }

    private long getRemoteFileSize(String fileUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(fileUrl).openConnection();
            connection.setRequestMethod("HEAD");
            connection.getInputStream(); // 일부 서버는 실제 요청이 있어야 Content-Length 반환
            return connection.getContentLengthLong();
        } catch (Exception e) {
            log.warn("⚠️ 파일 사이즈 확인 실패: {}", e.getMessage());
            return 0;
        }
    }
}