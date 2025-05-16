package com.capstone.meerkatai.storagespace.service;

import com.capstone.meerkatai.alarm.dto.AnomalyVideoMetadataRequest;
import com.capstone.meerkatai.global.service.S3Service;
import com.capstone.meerkatai.storagespace.entity.StorageSpace;
import com.capstone.meerkatai.storagespace.repository.StorageSpaceRepository;
import com.capstone.meerkatai.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageSpaceService {

    private final StorageSpaceRepository storageSpaceRepository;
    private final S3Service s3Service;

    public void updateUsedSpace(AnomalyVideoMetadataRequest request) {
        Long userId = request.getUserId();

        // 1. 사용자에 해당하는 저장공간 엔티티 조회
        StorageSpace storageSpace = storageSpaceRepository.findByUserUserId(userId)
                .orElseThrow(() -> new RuntimeException("저장공간 정보 없음"));

        // 2. 영상 URL로부터 파일 사이즈 추출
        long fileSizeInBytes = getS3FileSize(request.getVideoUrl());

        // 3. 기존 used_space에 더해서 갱신
        long currentUsed = storageSpace.getUsedSpace() != null ? storageSpace.getUsedSpace() : 0;
        storageSpace.setUsedSpace(currentUsed + fileSizeInBytes);

        // 4. 저장
        storageSpaceRepository.save(storageSpace);
        log.info("✅ 저장공간 갱신 완료: user={}, 추가 사용량={} byte", userId, fileSizeInBytes);
    }

    private long getS3FileSize(String fileUrl) {
        try {
            if (!s3Service.isS3Url(fileUrl)) {
                log.warn("S3 URL이 아닙니다: {}", fileUrl);
                return 0;
            }
            
            // 1. 객체 키 추출
            String objectKey = s3Service.extractS3Key(fileUrl);
            if (objectKey == null) {
                log.warn("S3 객체 키를 추출할 수 없습니다: {}", fileUrl);
                return 0;
            }
            
            // 2. S3 메타데이터 API로 파일 크기 가져오기
            long size = s3Service.getObjectSize(objectKey);
            if (size > 0) {
                log.info("S3 메타데이터에서 파일 크기 조회 성공: {} 바이트", size);
                return size;
            }
            
            // 3. 메타데이터로 조회 실패한 경우 Presigned URL로 시도
            log.info("메타데이터 조회 실패. Presigned URL로 파일 크기 조회 시도");
            URL presignedUrl = s3Service.generatePresignedUrlForDownload(objectKey);
            
            try {
                HttpURLConnection conn = (HttpURLConnection) presignedUrl.openConnection();
                conn.setRequestMethod("HEAD");
                long contentLength = conn.getContentLengthLong();
                if (contentLength > 0) {
                    log.info("Presigned URL로 파일 크기 조회 성공: {} 바이트", contentLength);
                    return contentLength;
                } else {
                    log.warn("Presigned URL로도 파일 크기를 가져올 수 없습니다.");
                }
            } catch (Exception e) {
                log.warn("Presigned URL로 파일 크기 조회 실패: {}", e.getMessage());
            }
            
            // 기본값: 평균 영상 크기 (5MB)
            long defaultSize = 5 * 1024 * 1024;
            log.info("파일 크기를 확인할 수 없어 기본값 사용: {} 바이트", defaultSize);
            return defaultSize;
        } catch (Exception e) {
            log.warn("⚠️ 파일 사이즈 확인 실패: {}", e.getMessage());
            // 기본값: 평균 영상 크기 (5MB)
            return 5 * 1024 * 1024;
        }
    }

    //로그인시 생성되는 저장공간 테이블 생성 메소드
    public void saveStorageSpace(User user) {

        Long totalSpace = 100L;
        Long usedSpace = 0L;

        StorageSpace storageSpace = StorageSpace.builder()
                .totalSpace(totalSpace)
                .usedSpace(usedSpace)
                .user(user)
                .build();

        // 4. 저장
        storageSpaceRepository.save(storageSpace);

        log.info("✅ 이상행동 저장 완료: anomaly_id={}", storageSpace.getStorageId());
    }
}