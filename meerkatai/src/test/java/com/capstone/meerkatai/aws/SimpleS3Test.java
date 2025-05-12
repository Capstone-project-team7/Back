package com.capstone.meerkatai.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 매우 간단한 AWS S3 연결 테스트
 */
public class SimpleS3Test {

    @Test
    void testS3Connection() {
        // 환경 변수에서 자격 증명 가져오기
        String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
        String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        
        System.out.println("==== AWS 자격 증명 확인 ====");
        System.out.println("ACCESS_KEY: " + (accessKey != null ? "설정됨" : "없음"));
        System.out.println("SECRET_KEY: " + (secretKey != null ? "설정됨" : "없음"));
        
        if (accessKey == null || secretKey == null || accessKey.isEmpty() || secretKey.isEmpty()) {
            System.out.println("AWS 자격 증명이 없어 테스트를 건너뜁니다.");
            return;
        }
        
        try {
            System.out.println("S3 클라이언트 생성 중...");
            
            // AWS 자격 증명 설정
            AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            
            // Amazon S3 클라이언트 생성
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(Regions.AP_NORTHEAST_2)
                    .build();
            
            System.out.println("S3 클라이언트 생성 완료");
            
            // 특정 버킷 존재 확인 (전체 버킷 나열 대신)
            String bucketName = "cctv-recordings-yuhan-20250505";
            boolean exists = s3Client.doesBucketExistV2(bucketName);
            assertTrue(exists, "버킷이 존재하지 않습니다: " + bucketName);
            
            System.out.println("\n버킷 '" + bucketName + "' 존재 확인: " + exists);
            
            // 특정 버킷 내의 객체 나열 (권한 테스트)
            System.out.println("\n==== 버킷 내 객체 목록 ====");
            s3Client.listObjects(bucketName).getObjectSummaries().forEach(item -> {
                System.out.println("- " + item.getKey());
            });
            
            System.out.println("\nS3 연결 테스트 성공!");
        } catch (Exception e) {
            System.out.println("\n==== 오류 발생 ====");
            System.out.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            
            // 상세 오류 추적
            if (e instanceof com.amazonaws.services.s3.model.AmazonS3Exception) {
                com.amazonaws.services.s3.model.AmazonS3Exception s3Exception = 
                    (com.amazonaws.services.s3.model.AmazonS3Exception) e;
                System.out.println("AWS 오류 코드: " + s3Exception.getErrorCode());
                System.out.println("AWS 오류 유형: " + s3Exception.getErrorType());
                System.out.println("AWS 요청 ID: " + s3Exception.getRequestId());
                System.out.println("AWS 확장 요청 ID: " + s3Exception.getExtendedRequestId());
            }
            
            throw e;
        }
    }
} 