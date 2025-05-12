package com.capstone.meerkatai.aws;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.capstone.meerkatai.global.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 테스트 환경용 S3Service 구현
 * TestAwsS3Configuration과 함께 사용됩니다.
 */
@Service
@Primary
@Profile("test")
public class TestS3Service extends S3Service {

    private static final Logger logger = LoggerFactory.getLogger(TestS3Service.class);
    
    private final AmazonS3Client amazonS3Client;
    
    @Value("${cloud.aws.s3.bucket:test-bucket}")
    private String bucketName;
    
    @Value("${aws.s3.video-prefix:clips/}")
    private String videoPrefix;
    
    @Value("${aws.s3.thumbnail-prefix:thumbnails/}")
    private String thumbnailPrefix;
    
    @Value("${aws.s3.presigned-url.expiration-minutes:10}")
    private int presignedUrlExpirationMinutes;
    
    public TestS3Service(AmazonS3Client amazonS3Client) {
        super(amazonS3Client);
        this.amazonS3Client = amazonS3Client;
        logger.info("TestS3Service 초기화 - S3 버킷: {}", bucketName);
    }
    
    @Override
    public String generateVideoKey(Long cctvId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return videoPrefix + cctvId + "_" + timestamp + ".mp4";
    }
    
    @Override
    public String generateThumbnailKey(String videoKey) {
        String videoName = videoKey.substring(videoPrefix.length());
        String thumbnailName = videoName.replaceAll("\\.mp4$", ".jpg");
        return thumbnailPrefix + thumbnailName;
    }
    
    @Override
    public URL generatePresignedUrlForUpload(String objectKey) {
        return generatePresignedUrl(objectKey, HttpMethod.PUT);
    }
    
    @Override
    public URL generatePresignedUrlForDownload(String objectKey) {
        return generatePresignedUrl(objectKey, HttpMethod.GET);
    }
    
    private URL generatePresignedUrl(String objectKey, HttpMethod httpMethod) {
        Date expiration = new Date();
        expiration.setTime(expiration.getTime() + TimeUnit.MINUTES.toMillis(presignedUrlExpirationMinutes));
        
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, objectKey)
                        .withMethod(httpMethod)
                        .withExpiration(expiration);
        
        URL url = amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest);
        logger.info("[테스트] Generated presigned URL: {}", url);
        return url;
    }
    
    @Override
    public void deleteObject(String objectKey) {
        logger.info("[테스트] 객체 삭제: s3://{}/{}", bucketName, objectKey);
        amazonS3Client.deleteObject(bucketName, objectKey);
    }
    
    @Override
    public String uploadFile(MultipartFile file, String objectKey) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            
            amazonS3Client.putObject(bucketName, objectKey, file.getInputStream(), metadata);
            logger.info("[테스트] 파일 업로드 완료: {}", objectKey);
            
            return "https://" + bucketName + ".s3.amazonaws.com/" + objectKey;
        } catch (IOException e) {
            logger.error("[테스트] 파일 업로드 실패", e);
            throw new RuntimeException("[테스트] 파일 업로드 실패", e);
        }
    }
} 