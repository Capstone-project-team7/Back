package com.capstone.meerkatai.global.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * AWS S3 서비스 클래스
 * <p>
 * 영상 파일 및 썸네일 관련 S3 작업을 처리합니다.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.video-prefix}")
    private String videoPrefix;

    @Value("${aws.s3.thumbnail-prefix}")
    private String thumbnailPrefix;

    @Value("${aws.s3.presigned-url.expiration-minutes}")
    private int presignedUrlExpirationMinutes;

    /**
     * 영상 파일 경로 생성
     * <p>
     * 형식: cctvId_yyyyMMdd_HHmmss.mp4
     * 예시: 456_20250508_191705.mp4
     * </p>
     * 
     * @param cctvId CCTV ID
     * @return S3 객체 키 (clips/456_20250508_191705.mp4 형식)
     */
    public String generateVideoKey(Long cctvId) {
        String timestamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return videoPrefix + cctvId + "_" + timestamp + ".mp4";
    }

    /**
     * 썸네일 경로 생성
     * <p>
     * 영상 파일과 동일한 이름 형식이지만 확장자가 .jpg
     * </p>
     * 
     * @param videoKey 영상 파일 키 (clips/456_20250508_191705.mp4 형식)
     * @return 썸네일 키 (thumbnails/456_20250508_191705.jpg 형식)
     */
    public String generateThumbnailKey(String videoKey) {
        String videoName = videoKey.substring(videoPrefix.length());
        String thumbnailName = videoName.replaceAll("\\.mp4$", ".jpg");
        return thumbnailPrefix + thumbnailName;
    }

    /**
     * 업로드용 presigned URL 생성
     * 
     * @param objectKey S3 객체 키
     * @return 업로드용 presigned URL
     */
    public URL generatePresignedUrlForUpload(String objectKey) {
        return generatePresignedUrl(objectKey, HttpMethod.PUT);
    }

    /**
     * 다운로드용 presigned URL 생성
     * 
     * @param objectKey S3 객체 키
     * @return 다운로드용 presigned URL
     */
    public URL generatePresignedUrlForDownload(String objectKey) {
        // URL 인코딩 문제 처리
        if (objectKey.startsWith("/")) {
            objectKey = objectKey.substring(1);
        }
        
        // %2F 등의 인코딩된 문자가 포함된 경우 디코딩
        if (objectKey.contains("%")) {
            objectKey = URLDecoder.decode(objectKey, StandardCharsets.UTF_8);
        }
        
        log.debug("프리사인드 URL 생성을 위한 객체 키 정규화: {}", objectKey);
        return generatePresignedUrl(objectKey, HttpMethod.GET);
    }

    /**
     * presigned URL 생성
     * 
     * @param objectKey S3 객체 키
     * @param httpMethod HTTP 메소드 (GET, PUT)
     * @return presigned URL
     */
    private URL generatePresignedUrl(String objectKey, HttpMethod httpMethod) {
        Date expiration = new Date();
        expiration.setTime(expiration.getTime() + TimeUnit.MINUTES.toMillis(presignedUrlExpirationMinutes));

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, objectKey)
                        .withMethod(httpMethod)
                        .withExpiration(expiration);

        URL url = amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest);
        log.info("Generated presigned URL: {}", url);
        return url;
    }
    
    /**
     * 객체 삭제
     * 
     * @param objectKey S3 객체 키
     */
    public void deleteObject(String objectKey) {
        // URL 인코딩 문제 처리
        if (objectKey.startsWith("/")) {
            objectKey = objectKey.substring(1);
        }
        
        // %2F 등의 인코딩된 문자가 포함된 경우 디코딩
        if (objectKey.contains("%")) {
            objectKey = URLDecoder.decode(objectKey, StandardCharsets.UTF_8);
        }
        
        amazonS3Client.deleteObject(bucketName, objectKey);
        log.info("Deleted object: s3://{}/{}", bucketName, objectKey);
    }
    
    /**
     * 파일 업로드
     * 
     * @param file MultipartFile 객체
     * @param objectKey S3 객체 키
     * @return 업로드된 파일의 URL
     */
    public String uploadFile(MultipartFile file, String objectKey) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            
            amazonS3Client.putObject(bucketName, objectKey, file.getInputStream(), metadata);
            
            return "https://" + bucketName + ".s3.amazonaws.com/" + objectKey;
        } catch (IOException e) {
            log.error("Failed to upload file to S3", e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }
    
    /**
     * URL이 S3 URL인지 확인
     * 
     * @param url 검증할 URL
     * @return S3 URL이면 true, 아니면 false
     */
    public boolean isS3Url(String url) {
        return url != null && url.contains("s3.") && url.contains("amazonaws.com");
    }
    
    /**
     * S3 URL에서 객체 키 추출
     * 
     * @param s3Url S3 URL (https://버킷명.s3.리전.amazonaws.com/객체키)
     * @return 객체 키
     */
    public String extractS3Key(String s3Url) {
        if (s3Url == null || !isS3Url(s3Url)) {
            log.warn("유효하지 않은 S3 URL: {}", s3Url);
            return null;
        }
        
        try {
            String objectKey;
            
            // 미리 알려진 접두사(prefix)를 통해 추출 시도
            if (s3Url.contains("/clips/")) {
                int startIndex = s3Url.indexOf("/clips/");
                objectKey = s3Url.substring(startIndex);
                // 첫 번째 '/'를 제거하여 올바른 객체 키 형식으로 변환
                if (objectKey.startsWith("/")) {
                    objectKey = objectKey.substring(1);
                }
            } else if (s3Url.contains("/thumbnails/")) {
                int startIndex = s3Url.indexOf("/thumbnails/");
                objectKey = s3Url.substring(startIndex);
                // 첫 번째 '/'를 제거하여 올바른 객체 키 형식으로 변환
                if (objectKey.startsWith("/")) {
                    objectKey = objectKey.substring(1);
                }
            } else {
                // 일반적인 방법으로 추출 시도
                int domainEndIndex = s3Url.indexOf(".amazonaws.com/");
                if (domainEndIndex != -1) {
                    objectKey = s3Url.substring(domainEndIndex + ".amazonaws.com/".length());
                } else {
                    throw new IllegalArgumentException("올바른 S3 URL 형식이 아닙니다: " + s3Url);
                }
            }
            
            // %2F 등의 인코딩된 문자가 포함된 경우 디코딩
            if (objectKey.contains("%")) {
                objectKey = URLDecoder.decode(objectKey, StandardCharsets.UTF_8);
            }
            
            log.debug("추출된 S3 객체 키: {}", objectKey);
            return objectKey;
        } catch (Exception e) {
            log.error("S3 객체 키 추출 실패: {}", e.getMessage());
            throw new IllegalArgumentException("S3 URL에서 객체 키를 추출할 수 없습니다: " + s3Url, e);
        }
    }
    
    /**
     * 비디오 URL에서 썸네일 URL 생성
     * 
     * @param videoUrl 비디오 URL (S3)
     * @return 썸네일 URL
     */
    public String generateThumbnailUrlFromVideoUrl(String videoUrl) {
        if (!isS3Url(videoUrl)) {
            log.warn("S3 URL이 아닌 영상 URL에서 썸네일 URL을 생성할 수 없습니다: {}", videoUrl);
            return null;
        }
        
        return videoUrl.replace("/clips/", "/thumbnails/").replaceAll("\\.mp4$", ".jpg");
    }
    
    /**
     * S3 객체 메타데이터 가져오기
     * 
     * @param objectKey S3 객체 키
     * @return 객체 메타데이터
     */
    public ObjectMetadata getObjectMetadata(String objectKey) {
        try {
            // URL 인코딩 문제 처리
            if (objectKey.startsWith("/")) {
                objectKey = objectKey.substring(1);
            }
            
            // %2F 등의 인코딩된 문자가 포함된 경우 디코딩
            if (objectKey.contains("%")) {
                objectKey = URLDecoder.decode(objectKey, StandardCharsets.UTF_8);
            }
            
            GetObjectMetadataRequest metadataRequest = new GetObjectMetadataRequest(bucketName, objectKey);
            ObjectMetadata metadata = amazonS3Client.getObjectMetadata(metadataRequest);
            log.info("S3 객체 메타데이터 조회 성공: key={}, contentLength={}", objectKey, metadata.getContentLength());
            return metadata;
        } catch (Exception e) {
            log.error("S3 객체 메타데이터 조회 실패: key={}, error={}", objectKey, e.getMessage());
            return null;
        }
    }
    
    /**
     * S3 객체의 파일 크기 가져오기
     * 
     * @param objectKey S3 객체 키
     * @return 파일 크기 (바이트)
     */
    public long getObjectSize(String objectKey) {
        ObjectMetadata metadata = getObjectMetadata(objectKey);
        if (metadata != null) {
            return metadata.getContentLength();
        }
        return 0;
    }
    
    /**
     * S3 객체의 콘텐츠 타입 가져오기
     * 
     * @param objectKey S3 객체 키
     * @return 콘텐츠 타입
     */
    public String getObjectContentType(String objectKey) {
        ObjectMetadata metadata = getObjectMetadata(objectKey);
        if (metadata != null) {
            return metadata.getContentType();
        }
        return null;
    }
    
    /**
     * S3 객체의 사용자 정의 메타데이터 가져오기
     * 
     * @param objectKey S3 객체 키
     * @return 사용자 정의 메타데이터 맵
     */
    public Map<String, String> getUserMetadata(String objectKey) {
        ObjectMetadata metadata = getObjectMetadata(objectKey);
        if (metadata != null) {
            return metadata.getUserMetadata();
        }
        return null;
    }
} 