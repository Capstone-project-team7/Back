package com.capstone.meerkatai.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.capstone.meerkatai.alarm.service.EmailService;
import com.capstone.meerkatai.global.service.S3Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * AWS S3 통합 테스트
 * 
 * 실제 AWS S3 서비스와 연결하여 기능을 테스트합니다.
 * 테스트 실행 전 환경 변수에 AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY가 설정되어 있어야 합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import({TestAwsS3Configuration.class, AwsS3IntegrationTest.TestConfig.class})
public class AwsS3IntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public JavaMailSender javaMailSender() {
            return mock(JavaMailSender.class);
        }
        
        @Bean
        @Primary
        public EmailService emailService() {
            // EmailService를 모킹하여 실제 이메일 전송을 방지
            return mock(EmailService.class);
        }
    }

    @Autowired
    private S3Service s3Service;
    
    @Autowired
    private AmazonS3 amazonS3;
    
    @Value("${cloud.aws.s3.bucket:cctv-recordings-yuhan-20250505}")
    private String bucketName;
    
    @Test
    void testS3Connection() {
        // 버킷이 존재하는지 확인
        boolean exists = amazonS3.doesBucketExistV2(bucketName);
        assertTrue(exists, "버킷이 존재해야 합니다: " + bucketName);
        
        System.out.println("AWS S3 버킷 연결 성공: " + bucketName);
    }
    
    @Test
    void testUploadDownloadDelete() throws IOException {
        // 테스트 파일 준비
        String objectKey = "test-integration-" + System.currentTimeMillis() + ".txt";
        String content = "AWS S3 통합 테스트 파일입니다.";
        
        // 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/plain");
        metadata.setContentLength(content.getBytes(StandardCharsets.UTF_8).length);
        
        try {
            // 파일 업로드
            amazonS3.putObject(bucketName, objectKey, 
                    new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), metadata);
            
            // 파일 존재 확인
            assertTrue(amazonS3.doesObjectExist(bucketName, objectKey), "업로드한 파일이 존재해야 합니다");
            
            // 파일 다운로드 및 내용 확인
            S3Object s3Object = amazonS3.getObject(bucketName, objectKey);
            assertNotNull(s3Object, "다운로드한 객체가 null이 아니어야 합니다");
            
            // Presigned URL 생성 테스트
            URL presignedUrl = s3Service.generatePresignedUrlForDownload(objectKey);
            assertNotNull(presignedUrl, "Presigned URL이 생성되어야 합니다");
            assertTrue(presignedUrl.toString().contains(objectKey), "URL에 객체 키가 포함되어야 합니다");
            
            System.out.println("생성된 Presigned URL: " + presignedUrl);
        } finally {
            // 테스트 후 정리
            if (amazonS3.doesObjectExist(bucketName, objectKey)) {
                amazonS3.deleteObject(bucketName, objectKey);
                System.out.println("테스트 파일 삭제 완료: " + objectKey);
            }
        }
    }
} 