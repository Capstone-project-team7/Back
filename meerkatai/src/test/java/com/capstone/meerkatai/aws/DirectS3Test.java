package com.capstone.meerkatai.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 매우 간단한 직접 AWS S3 연결 테스트
 */
public class DirectS3Test {
    
    // Python 코드와 동일한 버킷 이름과 리전 사용
    private static final String S3_BUCKET_NAME = "cctv-recordings-yuhan-20250505";
    private static final Regions AWS_REGION = Regions.AP_NORTHEAST_2;
    
    @Test
    void testDirectS3Connection() {
        // 환경 변수에서 자격 증명 가져오기
        String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
        String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        
        System.out.println("===== AWS 자격 증명 진단 =====");
        System.out.println("ACCESS_KEY가 설정되었나요? " + (accessKey != null && !accessKey.isEmpty()));
        System.out.println("SECRET_KEY가 설정되었나요? " + (secretKey != null && !secretKey.isEmpty()));
        
        // 자격 증명 없으면 테스트 무시
        if (accessKey == null || accessKey.isEmpty() || secretKey == null || secretKey.isEmpty()) {
            System.out.println("AWS 자격 증명이 설정되지 않았습니다. 테스트를 건너뜁니다.");
            return;
        }
        
        try {
            // 기본적인 S3 클라이언트 생성
            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(AWS_REGION)
                    .build();
            
            // 1. 버킷 목록 확인
            List<Bucket> buckets = s3Client.listBuckets();
            assertNotNull(buckets, "버킷 목록이 null입니다.");
            
            System.out.println("===== 버킷 목록 =====");
            for (Bucket bucket : buckets) {
                System.out.println("버킷 이름: " + bucket.getName());
            }
            
            // 2. 특정 버킷 존재 확인
            boolean bucketExists = s3Client.doesBucketExistV2(S3_BUCKET_NAME);
            System.out.println("\n버킷 '" + S3_BUCKET_NAME + "' 존재: " + bucketExists);
            
            if (bucketExists) {
                // 3. 간단한 테스트 파일 업로드
                String testKey = "test/simple_java_test_" + System.currentTimeMillis() + ".txt";
                String testContent = "Java에서 생성한 테스트 파일입니다. 시간: " + new java.util.Date();
                
                // 메타데이터 설정
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType("text/plain");
                metadata.setContentLength(testContent.getBytes(StandardCharsets.UTF_8).length);
                
                // 파일 업로드
                s3Client.putObject(
                    S3_BUCKET_NAME,
                    testKey,
                    new ByteArrayInputStream(testContent.getBytes(StandardCharsets.UTF_8)),
                    metadata
                );
                
                System.out.println("\n===== 테스트 파일 업로드 =====");
                System.out.println("파일 업로드 성공: s3://" + S3_BUCKET_NAME + "/" + testKey);
                
                // 업로드 확인
                boolean fileExists = s3Client.doesObjectExist(S3_BUCKET_NAME, testKey);
                assertTrue(fileExists, "업로드된 파일이 존재하지 않습니다.");
                System.out.println("파일 존재 확인: " + fileExists);
                
                // 파일 정보 확인
                if (fileExists) {
                    ObjectMetadata objMetadata = s3Client.getObjectMetadata(S3_BUCKET_NAME, testKey);
                    System.out.println("파일 크기: " + objMetadata.getContentLength() + " bytes");
                    System.out.println("Content Type: " + objMetadata.getContentType());
                    System.out.println("Last Modified: " + objMetadata.getLastModified());
                }
                
                // 간단한 목록 조회 (최대 5개만)
                System.out.println("\n===== 버킷 내 객체 목록 (최대 5개) =====");
                s3Client.listObjects(S3_BUCKET_NAME).getObjectSummaries().stream()
                        .limit(5)
                        .forEach(obj -> System.out.println("- " + obj.getKey() + " (" + obj.getSize() + " bytes)"));
            }
            
        } catch (Exception e) {
            System.out.println("\n===== 오류 발생 =====");
            System.out.println("메시지: " + e.getMessage());
            e.printStackTrace();
            fail("S3 연결 테스트 실패: " + e.getMessage());
        }
    }
} 