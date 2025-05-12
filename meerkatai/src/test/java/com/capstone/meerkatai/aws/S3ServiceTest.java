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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * S3Service 단위 테스트
 * AmazonS3Client를 모킹하여 실제 AWS에 연결하지 않고 테스트합니다.
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
public class S3ServiceTest {

    @Autowired
    private AmazonS3Client amazonS3Client;

    @Autowired
    private S3Service s3Service;

    @BeforeEach
    void setUp() throws Exception {
        // S3Service에 필요한 설정 주입
        ReflectionTestUtils.setField(s3Service, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(s3Service, "videoPrefix", "clips/");
        ReflectionTestUtils.setField(s3Service, "thumbnailPrefix", "thumbnails/");
        ReflectionTestUtils.setField(s3Service, "presignedUrlExpirationMinutes", 10);
        // AmazonS3Client 주입
        ReflectionTestUtils.setField(s3Service, "amazonS3Client", amazonS3Client);
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
        assertTrue(url.toString().contains("test-bucket"));
    }

    @Test
    void testGeneratePresignedUrlForDownload() {
        URL url = s3Service.generatePresignedUrlForDownload("test-object");
        assertTrue(url.toString().contains("test-bucket"));
    }

    @Test
    void testDeleteObject() {
        // 삭제 메서드 호출 - 모킹된 클라이언트이므로 실제 삭제는 발생하지 않음
        s3Service.deleteObject("test-object");
    }
} 