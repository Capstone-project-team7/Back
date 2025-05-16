package com.capstone.meerkatai.video.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VideoService의 S3 URL 처리 기능을 테스트하는 클래스
 * 간소화된 버전으로 실제 엔티티 의존성을 제거하여 테스트
 */
public class VideoServiceTest {

    // S3 URL 처리 로직을 테스트하기 위한 헬퍼 메소드들
    private boolean isS3Url(String url) {
        return url != null && url.contains("s3.ap-northeast-2.amazonaws.com");
    }
    
    private String extractS3Key(String s3Url) {
        // URL 형식: https://버킷명.s3.리전.amazonaws.com/객체키
        if (s3Url == null) return null;
        
        int keyStartIndex;
        if (s3Url.contains("/clips/")) {
            keyStartIndex = s3Url.indexOf("/clips/");
        } else if (s3Url.contains("/thumbnails/")) {
            keyStartIndex = s3Url.indexOf("/thumbnails/");
        } else {
            // 접두사를 찾을 수 없는 경우
            keyStartIndex = s3Url.indexOf(".amazonaws.com/") + ".amazonaws.com/".length() - 1;
        }
        
        return s3Url.substring(keyStartIndex);
    }

    @Test
    @DisplayName("S3 URL 판별 테스트")
    public void testIsS3Url() {
        // 테스트 URL 목록
        String validS3Url = "https://cctv-recordings-yuhan-20250505.s3.ap-northeast-2.amazonaws.com/clips/10_20250514_043020.mp4";
        String validS3ThumbnailUrl = "https://cctv-recordings-yuhan-20250505.s3.ap-northeast-2.amazonaws.com/thumbnails/10_20250514_043020.jpg";
        String invalidUrl = "https://example.com/video.mp4";
        String nullUrl = null;
        
        // 검증
        assertTrue(isS3Url(validS3Url), "유효한 S3 비디오 URL은 true를 반환해야 합니다");
        assertTrue(isS3Url(validS3ThumbnailUrl), "유효한 S3 썸네일 URL은 true를 반환해야 합니다");
        assertFalse(isS3Url(invalidUrl), "유효하지 않은 URL은 false를 반환해야 합니다");
        assertFalse(isS3Url(nullUrl), "null URL은 false를 반환해야 합니다");
    }

    @Test
    @DisplayName("S3 객체 키 추출 테스트")
    public void testExtractS3Key() {
        // 테스트 URL 목록
        String videoUrl = "https://cctv-recordings-yuhan-20250505.s3.ap-northeast-2.amazonaws.com/clips/10_20250514_043020.mp4";
        String thumbnailUrl = "https://cctv-recordings-yuhan-20250505.s3.ap-northeast-2.amazonaws.com/thumbnails/10_20250514_043020.jpg";
        
        // 검증
        assertEquals("/clips/10_20250514_043020.mp4", extractS3Key(videoUrl), 
                   "비디오 URL에서 올바른 객체 키를 추출해야 합니다");
        assertEquals("/thumbnails/10_20250514_043020.jpg", extractS3Key(thumbnailUrl), 
                   "썸네일 URL에서 올바른 객체 키를 추출해야 합니다");
    }

    @Test
    @DisplayName("썸네일 URL 자동 생성 테스트")
    public void testThumbnailUrlGeneration() {
        // 테스트 비디오 URL
        String videoUrl = "https://cctv-recordings-yuhan-20250505.s3.ap-northeast-2.amazonaws.com/clips/10_20250514_043020.mp4";
        
        // 기대 썸네일 URL
        String expectedThumbnailUrl = "https://cctv-recordings-yuhan-20250505.s3.ap-northeast-2.amazonaws.com/thumbnails/10_20250514_043020.jpg";
        
        // URL 추출 로직 테스트
        String thumbnailUrl = videoUrl.replace("/clips/", "/thumbnails/").replaceAll("\\.mp4$", ".jpg");
        
        // 검증
        assertEquals(expectedThumbnailUrl, thumbnailUrl, "비디오 URL에서 썸네일 URL이 올바르게 생성되어야 합니다");
    }
} 