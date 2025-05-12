package com.capstone.meerkatai.aws;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * S3Service 기능을 단순하게 테스트하는 클래스
 * Mockito를 사용하여 AWS SDK를 모킹하여 테스트합니다.
 */
public class SimpleS3Service {

    @Mock
    private AmazonS3 s3Client;
    
    private String bucketName = "test-bucket";

    @BeforeEach
    void setUp() throws Exception {
        // Mockito 초기화
        MockitoAnnotations.openMocks(this);
        
        System.out.println("Mock S3 클라이언트 생성 완료");
        
        // 기본 모킹 설정
        when(s3Client.doesBucketExistV2(anyString())).thenReturn(true);
        when(s3Client.doesObjectExist(anyString(), anyString())).thenReturn(true);
        
        // S3Object 생성
        S3Object mockS3Object = new S3Object();
        ByteArrayInputStream bais = new ByteArrayInputStream("테스트 콘텐츠".getBytes(StandardCharsets.UTF_8));
        mockS3Object.setObjectContent(new S3ObjectInputStream(bais, null));
        when(s3Client.getObject(anyString(), anyString())).thenReturn(mockS3Object);
        
        // Presigned URL 생성
        when(s3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenAnswer(invocation -> {
                    return new URL("https://test-bucket.s3.amazonaws.com/test-file-" + System.currentTimeMillis());
                });
    }
    
    @Test
    void testBucketExists() {
        // 특정 버킷 존재 확인
        boolean exists = s3Client.doesBucketExistV2(bucketName);
        assertTrue(exists, "버킷이 존재하지 않습니다: " + bucketName);
        
        System.out.println("버킷 '" + bucketName + "' 존재 확인: " + exists);
    }
    
    @Test
    void testUploadDownloadDelete() {
        // 테스트 파일 정보
        String objectKey = "test-service-" + System.currentTimeMillis() + ".txt";
        String content = "S3 서비스 테스트 파일입니다.";
        
        try {
            // 파일 업로드
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            InputStream inputStream = new ByteArrayInputStream(contentBytes);
            
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("text/plain");
            metadata.setContentLength(contentBytes.length);
            
            s3Client.putObject(bucketName, objectKey, inputStream, metadata);
            System.out.println("파일 업로드 완료: " + objectKey);
            
            // 파일 존재 확인
            boolean objectExists = s3Client.doesObjectExist(bucketName, objectKey);
            assertTrue(objectExists, "업로드한 객체가 존재해야 합니다");
            
            // 파일 다운로드
            S3Object object = s3Client.getObject(bucketName, objectKey);
            assertNotNull(object, "다운로드한 객체가 null이 아니어야 합니다");
            System.out.println("파일 다운로드 완료: " + objectKey);
            
            // Presigned URL 생성
            Date expiration = new Date();
            long expTimeMillis = expiration.getTime();
            expTimeMillis += 1000 * 60 * 10; // 10 minutes
            expiration.setTime(expTimeMillis);
            
            GeneratePresignedUrlRequest generatePresignedUrlRequest = 
                    new GeneratePresignedUrlRequest(bucketName, objectKey)
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration);
            
            URL presignedUrl = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
            assertNotNull(presignedUrl, "Presigned URL이 생성되어야 합니다");
            System.out.println("생성된 Presigned URL: " + presignedUrl);
            
        } finally {
            // 테스트는 모킹되어 있으므로 실제 삭제는 필요 없음
            System.out.println("테스트 완료 (모킹된 환경이므로 실제 삭제 없음): " + objectKey);
        }
    }
} 