package com.capstone.meerkatai.aws;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.capstone.meerkatai.global.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
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
public class S3ServiceTest {

    @Mock
    private AmazonS3Client amazonS3Client;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void setUp() throws Exception {
        // 수동으로 프로퍼티 설정
        ReflectionTestUtils.setField(s3Service, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(s3Service, "videoPrefix", "clips/");
        ReflectionTestUtils.setField(s3Service, "thumbnailPrefix", "thumbnails/");
        ReflectionTestUtils.setField(s3Service, "presignedUrlExpirationMinutes", 10);
        
        // Mock URL 반환 설정
        URL mockUrl = new URL("https://test-bucket.s3.amazonaws.com/test-object");
        when(amazonS3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(mockUrl);
    }

    @Test
    void testGenerateVideoKey() {
        String videoKey = s3Service.generateVideoKey(123L);
        assertTrue(videoKey.startsWith("clips/123_"), "Video key should start with clips/123_");
        assertTrue(videoKey.endsWith(".mp4"), "Video key should end with .mp4");
    }

    @Test
    void testGenerateThumbnailKey() {
        String thumbnailKey = s3Service.generateThumbnailKey("clips/123_20250508_191705.mp4");
        assertEquals("thumbnails/123_20250508_191705.jpg", thumbnailKey);
    }

    @Test
    void testGeneratePresignedUrlForUpload() {
        URL url = s3Service.generatePresignedUrlForUpload("test-object");
        assertEquals("https://test-bucket.s3.amazonaws.com/test-object", url.toString());
    }

    @Test
    void testGeneratePresignedUrlForDownload() {
        URL url = s3Service.generatePresignedUrlForDownload("test-object");
        assertEquals("https://test-bucket.s3.amazonaws.com/test-object", url.toString());
    }

    @Test
    void testDeleteObject() {
        // 검증할 것이 없지만 메소드가 호출되는지 확인
        s3Service.deleteObject("test-object");
        Mockito.verify(amazonS3Client).deleteObject("test-bucket", "test-object");
    }
} 