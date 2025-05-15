package com.capstone.meerkatai.global.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class S3ServiceTest {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Test
    @DisplayName("S3 버킷 연결 테스트")
    public void testS3Connection() {
        // 버킷이 존재하는지 확인
        boolean bucketExists = amazonS3Client.doesBucketExistV2(bucketName);
        assertTrue(bucketExists, "S3 버킷이 존재해야 합니다: " + bucketName);
    }

    @Test
    @DisplayName("영상 키 생성 테스트")
    public void testGenerateVideoKey() {
        // 테스트 CCTV ID
        Long cctvId = 10L;
        
        // 영상 키 생성
        String videoKey = s3Service.generateVideoKey(cctvId);
        
        // 검증
        assertTrue(videoKey.startsWith("clips/"));
        assertTrue(videoKey.contains("10_"));
        assertTrue(videoKey.endsWith(".mp4"));
        System.out.println("생성된 영상 키: " + videoKey);
    }

    @Test
    @DisplayName("썸네일 키 생성 테스트")
    public void testGenerateThumbnailKey() {
        // 테스트 영상 키
        String videoKey = "clips/10_20250514_043020.mp4";
        
        // 썸네일 키 생성
        String thumbnailKey = s3Service.generateThumbnailKey(videoKey);
        
        // 검증
        assertEquals("thumbnails/10_20250514_043020.jpg", thumbnailKey);
        System.out.println("생성된 썸네일 키: " + thumbnailKey);
    }

    @Test
    @DisplayName("업로드용 Presigned URL 생성 테스트")
    public void testGeneratePresignedUrlForUpload() {
        // 테스트 객체 키
        String objectKey = "test/test-upload-" + System.currentTimeMillis() + ".txt";
        
        // Presigned URL 생성
        URL presignedUrl = s3Service.generatePresignedUrlForUpload(objectKey);
        
        // 검증
        assertNotNull(presignedUrl);
        assertTrue(presignedUrl.toString().contains(bucketName));
        assertTrue(presignedUrl.toString().contains(objectKey));
        System.out.println("생성된 업로드용 Presigned URL: " + presignedUrl);
    }

    @Test
    @DisplayName("다운로드용 Presigned URL 생성 테스트")
    public void testGeneratePresignedUrlForDownload() {
        // 테스트 객체 키
        String objectKey = "test/test-download-" + System.currentTimeMillis() + ".txt";
        
        // Presigned URL 생성
        URL presignedUrl = s3Service.generatePresignedUrlForDownload(objectKey);
        
        // 검증
        assertNotNull(presignedUrl);
        assertTrue(presignedUrl.toString().contains(bucketName));
        assertTrue(presignedUrl.toString().contains(objectKey));
        System.out.println("생성된 다운로드용 Presigned URL: " + presignedUrl);
    }

    @Test
    @DisplayName("파일 업로드 및 삭제 테스트")
    public void testUploadAndDeleteFile() throws IOException {
        // 테스트 파일 생성
        String fileName = "test-file-" + System.currentTimeMillis() + ".txt";
        String objectKey = "test/" + fileName;
        String content = "이것은 테스트 파일입니다. 시간: " + System.currentTimeMillis();
        
        MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            fileName,
            "text/plain",
            content.getBytes(StandardCharsets.UTF_8)
        );
        
        try {
            // 파일 업로드
            String uploadedFileUrl = s3Service.uploadFile(mockFile, objectKey);
            
            // 검증
            assertNotNull(uploadedFileUrl);
            assertTrue(uploadedFileUrl.contains(bucketName));
            assertTrue(uploadedFileUrl.contains(objectKey));
            System.out.println("업로드된 파일 URL: " + uploadedFileUrl);
            
            // 파일이 실제로 존재하는지 확인
            boolean objectExists = amazonS3Client.doesObjectExist(bucketName, objectKey);
            assertTrue(objectExists, "업로드된 파일이 S3에 존재해야 합니다");
            
            // 파일 내용 확인
            S3Object s3Object = amazonS3Client.getObject(bucketName, objectKey);
            byte[] bytes = s3Object.getObjectContent().readAllBytes();
            String downloadedContent = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(content, downloadedContent, "업로드된 파일 내용이 일치해야 합니다");
            
        } finally {
            // 테스트 후 파일 삭제
            s3Service.deleteObject(objectKey);
            
            // 삭제 확인
            boolean objectExists = amazonS3Client.doesObjectExist(bucketName, objectKey);
            assertFalse(objectExists, "삭제된 파일은 S3에 존재하지 않아야 합니다");
        }
    }

    @Test
    @DisplayName("실제 영상 및 썸네일 경로 형식 테스트")
    public void testActualPathFormat() {
        // 테스트 CCTV ID
        Long cctvId = 10L;
        
        // 영상 키 생성
        String videoKey = s3Service.generateVideoKey(cctvId);
        
        // 썸네일 키 생성
        String thumbnailKey = s3Service.generateThumbnailKey(videoKey);
        
        // S3 URL 형식 생성
        String videoUrl = "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/" + videoKey;
        String thumbnailUrl = "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/" + thumbnailKey;
        
        // 검증
        assertTrue(videoUrl.matches("https://cctv-recordings-yuhan-20250505\\.s3\\.ap-northeast-2\\.amazonaws\\.com/clips/10_\\d{8}_\\d{6}\\.mp4"));
        assertTrue(thumbnailUrl.matches("https://cctv-recordings-yuhan-20250505\\.s3\\.ap-northeast-2\\.amazonaws\\.com/thumbnails/10_\\d{8}_\\d{6}\\.jpg"));
        
        System.out.println("영상 URL 형식: " + videoUrl);
        System.out.println("썸네일 URL 형식: " + thumbnailUrl);
    }
} 