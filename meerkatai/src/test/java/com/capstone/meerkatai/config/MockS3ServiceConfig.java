package com.capstone.meerkatai.config;

import com.capstone.meerkatai.global.service.S3Service;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 테스트 환경에서 사용할 S3Service의 모의 구현
 */
@Configuration
@Profile("test")
public class MockS3ServiceConfig {

    /**
     * 테스트용 S3Service Mock 객체 생성
     */
    @Bean
    @Primary
    public S3Service mockS3Service() throws MalformedURLException {
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