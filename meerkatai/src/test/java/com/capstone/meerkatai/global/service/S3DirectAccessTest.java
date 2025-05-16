package com.capstone.meerkatai.global.service;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * S3 직접 접근 테스트
 * 
 * Spring 컨텍스트 없이 AWS SDK를 직접 사용하여 S3 접근을 테스트합니다.
 * 이 테스트는 환경 변수나 AWS 자격 증명 파일을 통해 자격 증명이 설정되어 있어야 합니다.
 * - AWS_ACCESS_KEY_ID
 * - AWS_SECRET_ACCESS_KEY
 */
public class S3DirectAccessTest {

    private AmazonS3 amazonS3;
    private final String bucketName = "cctv-recordings-yuhan-20250505";
    private final String videoKey = "clips/10_20250514_043020.mp4";
    private final String region = "ap-northeast-2";

    @BeforeEach
    void setUp() {
        // AWS S3 클라이언트 직접 생성
        amazonS3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }

    @Test
    @DisplayName("S3 버킷 연결 테스트")
    public void testS3Connection() {
        try {
            // 버킷이 존재하는지 확인
            boolean bucketExists = amazonS3.doesBucketExistV2(bucketName);
            assertTrue(bucketExists, "S3 버킷이 존재해야 합니다: " + bucketName);
            System.out.println("S3 버킷 존재 확인 성공: " + bucketName);
        } catch (Exception e) {
            fail("S3 버킷 연결 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("특정 영상 파일 존재 테스트")
    public void testVideoFileExists() {
        try {
            // 특정 영상 파일이 존재하는지 확인
            boolean objectExists = amazonS3.doesObjectExist(bucketName, videoKey);
            assertTrue(objectExists, "영상 파일이 S3에 존재해야 합니다: " + videoKey);
            System.out.println("영상 파일 존재 확인 성공: " + videoKey);
        } catch (Exception e) {
            fail("영상 파일 존재 확인 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("영상 파일 메타데이터 확인 테스트")
    public void testVideoFileMetadata() {
        try {
            // 영상 파일의 메타데이터 확인
            var metadata = amazonS3.getObjectMetadata(bucketName, videoKey);
            assertNotNull(metadata, "영상 파일 메타데이터가 존재해야 합니다");
            
            // 파일 크기 출력
            System.out.println("영상 파일 크기: " + metadata.getContentLength() + " bytes");
            System.out.println("영상 파일 타입: " + metadata.getContentType());
            System.out.println("마지막 수정 시간: " + metadata.getLastModified());
            
            // 파일 크기가 0보다 커야 함
            assertTrue(metadata.getContentLength() > 0, "영상 파일 크기는 0보다 커야 합니다");
        } catch (Exception e) {
            fail("영상 파일 메타데이터 확인 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("영상 파일 다운로드 테스트")
    public void testVideoFileDownload() {
        try {
            // 영상 파일 객체 가져오기
            S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucketName, videoKey));
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
            System.out.println("영상 파일 다운로드 테스트 성공");
        } catch (Exception e) {
            fail("영상 파일 다운로드 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 