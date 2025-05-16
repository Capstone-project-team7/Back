package com.capstone.meerkatai.global.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class S3VideoAccessTest {

    @Autowired
    private AmazonS3 amazonS3;
    
    @Autowired
    private AmazonS3Client amazonS3Client;
    
    @Autowired
    private S3Service s3Service;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final String videoKey = "clips/10_20250514_043020.mp4";
    private final String videoUrl = "https://cctv-recordings-yuhan-20250505.s3.ap-northeast-2.amazonaws.com/clips/10_20250514_043020.mp4";

    @Test
    @DisplayName("S3 버킷 연결 테스트")
    public void testS3Connection() {
        // 버킷이 존재하는지 확인
        boolean bucketExists = amazonS3Client.doesBucketExistV2(bucketName);
        assertTrue(bucketExists, "S3 버킷이 존재해야 합니다: " + bucketName);
        System.out.println("S3 버킷 존재 확인: " + bucketName);
    }

    @Test
    @DisplayName("특정 영상 파일 존재 테스트")
    public void testVideoFileExists() {
        // 특정 영상 파일이 존재하는지 확인
        boolean objectExists = amazonS3Client.doesObjectExist(bucketName, videoKey);
        assertTrue(objectExists, "영상 파일이 S3에 존재해야 합니다: " + videoKey);
        System.out.println("영상 파일 존재 확인: " + videoKey);
    }

    @Test
    @DisplayName("영상 파일 메타데이터 확인 테스트")
    public void testVideoFileMetadata() {
        // 영상 파일의 메타데이터 확인
        var metadata = amazonS3Client.getObjectMetadata(bucketName, videoKey);
        assertNotNull(metadata, "영상 파일 메타데이터가 존재해야 합니다");
        
        // 파일 크기 출력
        System.out.println("영상 파일 크기: " + metadata.getContentLength() + " bytes");
        System.out.println("영상 파일 타입: " + metadata.getContentType());
        System.out.println("마지막 수정 시간: " + metadata.getLastModified());
        
        // 파일 크기가 0보다 커야 함
        assertTrue(metadata.getContentLength() > 0, "영상 파일 크기는 0보다 커야 합니다");
    }

    @Test
    @DisplayName("영상 파일 다운로드 테스트")
    public void testVideoFileDownload() throws IOException {
        try {
            // 영상 파일 객체 가져오기
            S3Object s3Object = amazonS3Client.getObject(bucketName, videoKey);
            assertNotNull(s3Object, "영상 파일 객체가 존재해야 합니다");
            
            // 스트림 가져오기
            S3ObjectInputStream objectContent = s3Object.getObjectContent();
            assertNotNull(objectContent, "영상 파일 스트림이 존재해야 합니다");
            
            // 처음 몇 바이트만 읽어서 파일이 손상되지 않았는지 확인
            byte[] buffer = new byte[1024]; // 1KB만 읽음
            int bytesRead = objectContent.read(buffer);
            
            assertTrue(bytesRead > 0, "영상 파일에서 데이터를 읽을 수 있어야 합니다");
            System.out.println("영상 파일에서 " + bytesRead + " 바이트를 성공적으로 읽었습니다");
            
            // 리소스 정리
            objectContent.close();
        } catch (Exception e) {
            fail("영상 파일 다운로드 중 예외 발생: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Presigned URL을 통한 영상 파일 접근 테스트")
    public void testVideoFileAccessViaPresignedUrl() {
        try {
            // 다운로드용 Presigned URL 생성
            URL presignedUrl = s3Service.generatePresignedUrlForDownload(videoKey);
            assertNotNull(presignedUrl, "Presigned URL이 생성되어야 합니다");
            
            System.out.println("생성된 Presigned URL: " + presignedUrl);
            
            // URL 연결 테스트
            HttpURLConnection connection = (HttpURLConnection) presignedUrl.openConnection();
            connection.setRequestMethod("GET");
            
            int responseCode = connection.getResponseCode();
            assertEquals(200, responseCode, "Presigned URL을 통한 접근이 성공해야 합니다 (HTTP 200)");
            
            // 콘텐츠 타입 확인
            String contentType = connection.getContentType();
            assertNotNull(contentType, "콘텐츠 타입이 존재해야 합니다");
            assertTrue(contentType.startsWith("video/"), "콘텐츠 타입은 video/로 시작해야 합니다");
            
            System.out.println("Presigned URL을 통한 영상 파일 접근 성공");
            System.out.println("콘텐츠 타입: " + contentType);
            System.out.println("콘텐츠 길이: " + connection.getContentLength() + " bytes");
        } catch (Exception e) {
            fail("Presigned URL을 통한 영상 파일 접근 중 예외 발생: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("직접 URL을 통한 영상 파일 접근 테스트")
    public void testVideoFileAccessViaDirectUrl() {
        try {
            // 직접 URL 연결 테스트
            URL directUrl = new URL(videoUrl);
            HttpURLConnection connection = (HttpURLConnection) directUrl.openConnection();
            connection.setRequestMethod("GET");
            
            int responseCode = connection.getResponseCode();
            System.out.println("직접 URL 응답 코드: " + responseCode);
            
            // S3 버킷이 공개 접근 가능하지 않은 경우 403 Forbidden이 예상됨
            // 이 경우 테스트는 실패하지만 정보를 제공함
            if (responseCode == 200) {
                System.out.println("직접 URL을 통한 영상 파일 접근 성공 (버킷이 공개 접근 가능)");
                System.out.println("콘텐츠 타입: " + connection.getContentType());
                System.out.println("콘텐츠 길이: " + connection.getContentLength() + " bytes");
            } else if (responseCode == 403) {
                System.out.println("직접 URL을 통한 영상 파일 접근 실패 (버킷이 비공개, 예상된 결과)");
                System.out.println("S3 파일에 접근하려면 Presigned URL이나 적절한 IAM 권한이 필요합니다");
            } else {
                fail("예상치 못한 응답 코드: " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("직접 URL을 통한 영상 파일 접근 중 예외 발생: " + e.getMessage());
            System.out.println("이는 버킷이 비공개인 경우 예상된 결과입니다");
        }
    }
} 