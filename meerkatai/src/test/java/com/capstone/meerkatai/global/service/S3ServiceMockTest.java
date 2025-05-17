package com.capstone.meerkatai.global.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class S3ServiceMockTest {

    @Mock
    private AmazonS3Client amazonS3Client;

    @InjectMocks
    private S3Service s3Service;

    private final String bucketName = "cctv-recordings-yuhan-20250505";
    private final String videoPrefix = "clips/";
    private final String thumbnailPrefix = "thumbnails/";
    private final int presignedUrlExpirationMinutes = 10;

    @BeforeEach
    void setUp() {
        // S3Service의 필드 값을 설정
        ReflectionTestUtils.setField(s3Service, "bucketName", bucketName);
        ReflectionTestUtils.setField(s3Service, "videoPrefix", videoPrefix);
        ReflectionTestUtils.setField(s3Service, "thumbnailPrefix", thumbnailPrefix);
        ReflectionTestUtils.setField(s3Service, "presignedUrlExpirationMinutes", presignedUrlExpirationMinutes);
    }

    @Test
    @DisplayName("영상 키 생성 테스트")
    public void testGenerateVideoKey() {
        // 테스트 CCTV ID
        Long cctvId = 10L;
        
        // 영상 키 생성
        String videoKey = s3Service.generateVideoKey(cctvId);
        
        // 검증
        assertTrue(videoKey.startsWith(videoPrefix));
        assertTrue(videoKey.contains(cctvId.toString() + "_"));
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
    @DisplayName("업로드용 Presigned URL 생성 테스트 (모킹)")
    public void testGeneratePresignedUrlForUpload() throws Exception {
        // 테스트 객체 키
        String objectKey = "test/test-upload.txt";
        
        // 모킹된 URL 생성
        URL mockUrl = new URL("https://" + bucketName + ".s3.amazonaws.com/" + objectKey);
        
        // amazonS3Client.generatePresignedUrl() 메서드 모킹
        when(amazonS3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class))).thenReturn(mockUrl);
        
        // Presigned URL 생성
        URL presignedUrl = s3Service.generatePresignedUrlForUpload(objectKey);
        
        // 검증
        assertNotNull(presignedUrl);
        assertEquals(mockUrl, presignedUrl);
        verify(amazonS3Client, times(1)).generatePresignedUrl(any(GeneratePresignedUrlRequest.class));
    }

    @Test
    @DisplayName("다운로드용 Presigned URL 생성 테스트 (모킹)")
    public void testGeneratePresignedUrlForDownload() throws Exception {
        // 테스트 객체 키
        String objectKey = "clips/10_20250514_043020.mp4";
        
        // 모킹된 URL 생성
        URL mockUrl = new URL("https://" + bucketName + ".s3.amazonaws.com/" + objectKey);
        
        // amazonS3Client.generatePresignedUrl() 메서드 모킹
        when(amazonS3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class))).thenReturn(mockUrl);
        
        // Presigned URL 생성
        URL presignedUrl = s3Service.generatePresignedUrlForDownload(objectKey);
        
        // 검증
        assertNotNull(presignedUrl);
        assertEquals(mockUrl, presignedUrl);
        verify(amazonS3Client, times(1)).generatePresignedUrl(any(GeneratePresignedUrlRequest.class));
    }

    @Test
    @DisplayName("파일 업로드 테스트 (모킹)")
    public void testUploadFile() throws IOException {
        // 테스트 파일 생성
        String fileName = "test-file.txt";
        String objectKey = "test/" + fileName;
        String content = "이것은 테스트 파일입니다.";
        
        MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            fileName,
            "text/plain",
            content.getBytes(StandardCharsets.UTF_8)
        );
        
        // 입력 스트림 모킹
        InputStream mockInputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        
        // MockMultipartFile.getInputStream() 결과를 모킹할 수 없으므로 실제 파일 사용
        
        // 파일 업로드 후 반환되는 URL 확인
        String uploadedFileUrl = s3Service.uploadFile(mockFile, objectKey);
        
        // 검증
        assertEquals("https://" + bucketName + ".s3.amazonaws.com/" + objectKey, uploadedFileUrl);
        
        // amazonS3Client.putObject() 호출 검증
        verify(amazonS3Client, times(1)).putObject(
            eq(bucketName), 
            eq(objectKey), 
            any(InputStream.class), 
            any(ObjectMetadata.class)
        );
    }

    @Test
    @DisplayName("파일 삭제 테스트 (모킹)")
    public void testDeleteObject() {
        // 테스트 객체 키
        String objectKey = "test/test-file.txt";
        
        // amazonS3Client.deleteObject() 메서드 모킹 (void 메서드)
        doNothing().when(amazonS3Client).deleteObject(bucketName, objectKey);
        
        // 파일 삭제
        s3Service.deleteObject(objectKey);
        
        // 검증
        verify(amazonS3Client, times(1)).deleteObject(bucketName, objectKey);
    }
}