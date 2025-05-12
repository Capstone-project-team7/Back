package com.capstone.meerkatai.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.capstone.meerkatai.global.config.AwsS3Config;
import com.capstone.meerkatai.global.service.S3Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AWS S3 통합 테스트
 * 
 * 주의: 이 테스트는 실제 AWS S3 서비스와 연결됩니다.
 * 테스트 전에 환경 변수 또는 application-test.properties에 적절한 AWS 자격 증명이 설정되어 있어야 합니다.
 */
@SpringBootTest
@ActiveProfiles("test") // test 프로필 사용
@Import({AwsS3Config.class, TestAwsS3Configuration.class}) // AWS S3 설정 클래스 명시적으로 포함
public class AwsS3IntegrationTest {

    @Autowired
    private S3Service s3Service;
    
    @Autowired
    private AmazonS3 amazonS3;
    
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    
    @Test
    void testS3Connection() {
        // 버킷이 존재하는지 확인
        boolean exists = amazonS3.doesBucketExistV2(bucketName);
        assertTrue(exists, "Bucket should exist: " + bucketName);
        
        System.out.println("Successfully connected to AWS S3 bucket: " + bucketName);
    }
    
    @Test
    void testGenerateVideoKey() {
        // 비디오 키 생성 테스트
        String videoKey = s3Service.generateVideoKey(12345L);
        assertNotNull(videoKey);
        assertTrue(videoKey.startsWith("clips/12345_"));
        assertTrue(videoKey.endsWith(".mp4"));
        
        System.out.println("Generated video key: " + videoKey);
    }
    
    @Test
    void testGeneratePresignedUrl() {
        // Presigned URL 생성 테스트
        String objectKey = "test-" + System.currentTimeMillis() + ".txt";
        URL presignedUrl = s3Service.generatePresignedUrlForUpload(objectKey);
        
        assertNotNull(presignedUrl);
        assertTrue(presignedUrl.toString().contains(bucketName));
        assertTrue(presignedUrl.toString().contains(objectKey));
        
        System.out.println("Generated presigned URL: " + presignedUrl);
    }
    
    @Test
    void testFileUploadAndDelete() throws IOException {
        // 테스트 파일 생성
        String objectKey = "test-file-" + System.currentTimeMillis() + ".txt";
        String content = "This is a test file for S3 upload";
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            content.getBytes(StandardCharsets.UTF_8)
        );
        
        // 직접 S3에 업로드
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        
        // 파일 업로드
        amazonS3.putObject(bucketName, objectKey, 
                         new ByteArrayInputStream(file.getBytes()), metadata);
        
        // 파일이 존재하는지 확인
        assertTrue(amazonS3.doesObjectExist(bucketName, objectKey));
        
        // 파일 내용 확인
        S3Object s3Object = amazonS3.getObject(bucketName, objectKey);
        assertNotNull(s3Object);
        
        // 테스트 후 파일 삭제
        s3Service.deleteObject(objectKey);
        
        // 삭제 확인
        assertFalse(amazonS3.doesObjectExist(bucketName, objectKey));
        
        System.out.println("Successfully uploaded, verified and deleted test file: " + objectKey);
    }
} 