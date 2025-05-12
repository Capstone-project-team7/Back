package com.capstone.meerkatai.aws;

import com.capstone.meerkatai.global.service.S3Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * S3Service 통합 테스트 - MockAwsConfiguration을 사용하여 AWS 서비스를 모킹
 */
@SpringBootTest(classes = {MockAwsConfiguration.class, S3Service.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "cloud.aws.region.static=ap-northeast-2",
    "aws.s3.bucket-name=test-bucket",
    "cloud.aws.s3.bucket=test-bucket",
    "aws.s3.presigned-url.expiration-minutes=10",
    "aws.s3.video-prefix=clips/",
    "aws.s3.thumbnail-prefix=thumbnails/",
    "cloud.aws.stack.auto=false"
})
public class S3ServiceIntegrationTest {

    @Autowired
    private S3Service s3Service;

    @Test
    void testS3ServiceInstance() {
        // S3Service가 정상적으로 주입되었는지 확인
        assertNotNull(s3Service, "S3Service 빈이 주입되어야 합니다");
        System.out.println("S3Service 클래스: " + s3Service.getClass().getName());
    }
    
    @Test
    void testGenerateVideoKey() {
        // 영상 키 생성 테스트
        Long cctvId = 123L;
        String videoKey = s3Service.generateVideoKey(cctvId);
        
        assertNotNull(videoKey, "생성된 비디오 키는 null이 아니어야 합니다");
        assertTrue(videoKey.startsWith("clips/"), "비디오 키는 'clips/'로 시작해야 합니다");
        assertTrue(videoKey.contains("123_"), "비디오 키는 CCTV ID를 포함해야 합니다");
        assertTrue(videoKey.endsWith(".mp4"), "비디오 키는 '.mp4' 확장자로 끝나야 합니다");
        
        System.out.println("생성된 비디오 키: " + videoKey);
    }
    
    @Test
    void testGenerateThumbnailKey() {
        // 썸네일 키 생성 테스트
        String videoKey = "clips/123_20250508_191705.mp4";
        String thumbnailKey = s3Service.generateThumbnailKey(videoKey);
        
        assertNotNull(thumbnailKey, "생성된 썸네일 키는 null이 아니어야 합니다");
        assertEquals("thumbnails/123_20250508_191705.jpg", thumbnailKey, 
                "썸네일 키는 비디오 키에서 경로 접두사와 확장자만 변경되어야 합니다");
        
        System.out.println("생성된 썸네일 키: " + thumbnailKey);
    }
    
    @Test
    void testGeneratePresignedUrls() {
        // Presigned URL 생성 테스트
        String objectKey = "clips/123_20250508_191705.mp4";
        
        URL uploadUrl = s3Service.generatePresignedUrlForUpload(objectKey);
        URL downloadUrl = s3Service.generatePresignedUrlForDownload(objectKey);
        
        assertNotNull(uploadUrl, "업로드용 Presigned URL은 null이 아니어야 합니다");
        assertNotNull(downloadUrl, "다운로드용 Presigned URL은 null이 아니어야 합니다");
        
        System.out.println("업로드용 Presigned URL: " + uploadUrl);
        System.out.println("다운로드용 Presigned URL: " + downloadUrl);
    }
} 