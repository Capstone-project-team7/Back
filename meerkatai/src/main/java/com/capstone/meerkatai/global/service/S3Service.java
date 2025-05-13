package com.capstone.meerkatai.global.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
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
} 