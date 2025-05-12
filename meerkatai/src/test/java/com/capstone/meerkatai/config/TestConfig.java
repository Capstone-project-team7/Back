package com.capstone.meerkatai.config;

import com.capstone.meerkatai.global.service.S3Service;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 통합 테스트를 제외한 단위 테스트 환경에서 사용할 Mock 설정 클래스
 */
@TestConfiguration
public class TestConfig {
    
    /**
     * 테스트용 JavaMailSender Mock 객체
     */
    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return Mockito.mock(JavaMailSender.class);
    }

    /**
     * 테스트용 S3Service Mock 객체 (통합 테스트 제외)
     * 단위 테스트에서만 사용되는 Mock 객체이며, 
     * AWS S3 통합 테스트에서는 실제 S3Service 빈을 사용합니다.
     */
    @Bean
    @Primary
    @Profile("!test") // test 프로파일이 아닐 때만 활성화
    public S3Service s3Service() throws MalformedURLException {
        S3Service mockS3Service = Mockito.mock(S3Service.class);
        
        // generateVideoKey 모의 구현
        Mockito.when(mockS3Service.generateVideoKey(Mockito.anyLong()))
                .thenAnswer(invocation -> {
                    Long cctvId = invocation.getArgument(0);
                    return "clips/" + cctvId + "_20250508_191705.mp4";
                });
        
        // generateThumbnailKey 모의 구현
        Mockito.when(mockS3Service.generateThumbnailKey(Mockito.anyString()))
                .thenAnswer(invocation -> {
                    String videoKey = invocation.getArgument(0);
                    return "thumbnails/" + videoKey.substring(6).replace(".mp4", ".jpg");
                });
        
        // generatePresignedUrlForUpload 모의 구현
        Mockito.when(mockS3Service.generatePresignedUrlForUpload(Mockito.anyString()))
                .thenAnswer(invocation -> {
                    String objectKey = invocation.getArgument(0);
                    return new URL("https://example.com/upload/" + objectKey);
                });
        
        // generatePresignedUrlForDownload 모의 구현
        Mockito.when(mockS3Service.generatePresignedUrlForDownload(Mockito.anyString()))
                .thenAnswer(invocation -> {
                    String objectKey = invocation.getArgument(0);
                    return new URL("https://example.com/download/" + objectKey);
                });
        
        // deleteObject 모의 구현 (아무 동작도 하지 않음)
        Mockito.doNothing().when(mockS3Service).deleteObject(Mockito.anyString());
        
        return mockS3Service;
    }
} 