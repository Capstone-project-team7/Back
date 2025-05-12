package com.capstone.meerkatai.storagespace.service;

import com.capstone.meerkatai.alarm.dto.AnomalyVideoMetadataRequest;
import com.capstone.meerkatai.storagespace.entity.StorageSpace;
import com.capstone.meerkatai.storagespace.repository.StorageSpaceRepository;
import com.capstone.meerkatai.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageSpaceService {

    private final StorageSpaceRepository storageSpaceRepository;
    
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    /**
     * 영상 저장 시 사용 공간 업데이트
     * 
     * @param request 이상행동 비디오 메타데이터 요청
     */
    @Transactional
    public void updateUsedSpace(AnomalyVideoMetadataRequest request) {
        Long userId = request.getUserId();

        // 1. 사용자에 해당하는 저장공간 엔티티 조회
        StorageSpace storageSpace = storageSpaceRepository.findByUserUserId(userId)
                .orElseThrow(() -> new RuntimeException("저장공간 정보 없음"));

        // 2. 영상 URL로부터 파일 사이즈 추출 (가능한 경우)
        long videoSizeInBytes = 0;
        if (request.getVideoUrl() != null && !request.getVideoUrl().isEmpty()) {
            videoSizeInBytes = getRemoteFileSize(request.getVideoUrl());
        } else {
            // 영상 URL이 없는 경우 예상 사이즈 추정 (5MB)
            videoSizeInBytes = 5 * 1024 * 1024;
        }
        
        // 3. 썸네일 사이즈 추정 (일반적으로 50KB)
        long thumbnailSizeInBytes = 50 * 1024;
        
        // 4. 총 추가 사용량 계산
        long totalAddedBytes = videoSizeInBytes + thumbnailSizeInBytes;

        // 5. 기존 used_space에 더해서 갱신
        long currentUsed = storageSpace.getUsedSpace() != null ? storageSpace.getUsedSpace() : 0;
        storageSpace.setUsedSpace(currentUsed + totalAddedBytes);

        // 6. 저장
        storageSpaceRepository.save(storageSpace);
        log.info("✅ 저장공간 갱신 완료: user={}, 추가 사용량={}MB", 
                userId, String.format("%.2f", totalAddedBytes / (1024.0 * 1024.0)));
    }

    /**
     * 원격 파일의 사이즈를 조회합니다.
     * 
     * @param fileUrl 원격 파일 URL
     * @return 파일 사이즈 (바이트)
     */
    private long getRemoteFileSize(String fileUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(fileUrl).openConnection();
            connection.setRequestMethod("HEAD");
            connection.getInputStream(); // 일부 서버는 실제 요청이 있어야 Content-Length 반환
            return connection.getContentLengthLong();
        } catch (Exception e) {
            log.warn("⚠️ 파일 사이즈 확인 실패: {}", e.getMessage());
            // 확인 실패시 기본값 (5MB) 반환
            return 5 * 1024 * 1024;
        }
    }

    /**
     * 새 사용자 저장공간을 생성합니다.
     * 
     * @param user 저장공간을 생성할 사용자
     */
    @Transactional
    public void saveStorageSpace(User user) {
        // 1. 기본 저장 공간 설정 (10GB)
        Long totalSpace = 10L * 1024 * 1024 * 1024; // 10GB
        Long usedSpace = 0L;

        // 2. 저장공간 엔티티 생성
        StorageSpace storageSpace = StorageSpace.builder()
                .totalSpace(totalSpace)
                .usedSpace(usedSpace)
                .user(user)
                .build();

        // 3. 저장
        storageSpaceRepository.save(storageSpace);

        log.info("✅ 저장공간 생성 완료: user_id={}, 총 용량={}GB", 
                user.getUserId(), totalSpace / (1024 * 1024 * 1024));
    }
}