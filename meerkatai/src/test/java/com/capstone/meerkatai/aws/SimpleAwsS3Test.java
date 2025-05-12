package com.capstone.meerkatai.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 단순한 AWS S3 연결 테스트
 * Spring 설정 없이 직접 AWS SDK를 사용하여 연결을 테스트합니다.
 */
public class SimpleAwsS3Test {
    
    // Python 코드와 동일한 버킷 이름과 리전 사용
    private static final String S3_BUCKET_NAME = "cctv-recordings-yuhan-20250505";
    private static final Regions AWS_REGION = Regions.AP_NORTHEAST_2;
    
    @Test
    void testS3ConnectionWithDirectSdk() {
        // 자격 증명 정보 출력
        printAwsCredentialsInfo();
        
        // 환경 변수에서 자격 증명 가져오기
        String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
        String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        
        // 자격 증명 진단
        System.out.println("=== AWS 자격 증명 진단 ===");
        System.out.println("ACCESS_KEY가 설정되었나요? " + (accessKey != null && !accessKey.isEmpty()));
        System.out.println("SECRET_KEY가 설정되었나요? " + (secretKey != null && !secretKey.isEmpty()));
        
        // 자격 증명이 없으면 테스트 건너뛰기
        if (accessKey == null || accessKey.isEmpty() || secretKey == null || secretKey.isEmpty()) {
            System.out.println("AWS 자격 증명이 설정되지 않았습니다. 테스트를 건너뜁니다.");
            System.out.println("환경 변수 AWS_ACCESS_KEY_ID와 AWS_SECRET_ACCESS_KEY를 설정하거나, 테스트 코드에 직접 입력하세요.");
            return;
        }
        
        try {
            // AWS S3 클라이언트 생성
            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(AWS_REGION)
                    .build();
            
            // 연결 테스트: 버킷 리스트 가져오기
            List<Bucket> buckets = s3Client.listBuckets();
            assertNotNull(buckets, "Bucket list should not be null");
            
            // 결과 출력
            System.out.println("===== AWS S3 Bucket List =====");
            for (Bucket b : buckets) {
                System.out.println("Bucket Name: " + b.getName());
            }
            
            // 특정 버킷 존재 확인
            boolean exists = s3Client.doesBucketExistV2(S3_BUCKET_NAME);
            System.out.println("Bucket '" + S3_BUCKET_NAME + "' exists: " + exists);
            
            // 버킷이 존재하면 객체 목록 표시
            if (exists) {
                System.out.println("\n===== Objects in Bucket: " + S3_BUCKET_NAME + " =====");
                s3Client.listObjects(S3_BUCKET_NAME).getObjectSummaries().stream()
                        .limit(10) // 처음 10개만 표시
                        .forEach(object -> System.out.println("Object Key: " + object.getKey()));
            }
            
            // 테스트 파일 업로드 (boto3 코드와 유사하게)
            testS3Upload(s3Client);
            
        } catch (Exception e) {
            System.out.println("AWS S3 연결 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            fail("AWS S3 연결 실패: " + e.getMessage());
        }
    }
    
    /**
     * boto3 코드의 test_s3_connection 함수와 유사한 기능 구현
     */
    private void testS3Upload(AmazonS3 s3Client) {
        try {
            // 테스트 파일 업로드 (PutObject 권한 필요)
            System.out.println("\n===== 테스트 파일 업로드 =====");
            String testData = "This is a test file for S3 connection.";
            String testKey = "test/connection_test_java.txt";
            
            // 메타데이터 설정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("text/plain");
            metadata.setContentLength(testData.getBytes(StandardCharsets.UTF_8).length);
            
            // 파일 업로드
            s3Client.putObject(
                new PutObjectRequest(
                    S3_BUCKET_NAME,
                    testKey,
                    new ByteArrayInputStream(testData.getBytes(StandardCharsets.UTF_8)),
                    metadata
                )
            );
            
            System.out.println("S3 테스트 파일 업로드 성공: s3://" + S3_BUCKET_NAME + "/" + testKey);
            
            // 업로드 확인
            boolean exists = s3Client.doesObjectExist(S3_BUCKET_NAME, testKey);
            System.out.println("테스트 파일 존재 확인: " + exists);
            
            if (exists) {
                // 객체 정보 확인
                ObjectMetadata objMetadata = s3Client.getObjectMetadata(S3_BUCKET_NAME, testKey);
                System.out.println("파일 크기: " + objMetadata.getContentLength() + " bytes");
                System.out.println("Content Type: " + objMetadata.getContentType());
                System.out.println("Last Modified: " + objMetadata.getLastModified());
            }
            
        } catch (Exception e) {
            System.out.println("테스트 파일 업로드 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * boto3 코드의 print_aws_credentials_info 함수와 유사한 기능 구현
     */
    private void printAwsCredentialsInfo() {
        System.out.println("===== AWS 자격 증명 정보 =====");
        
        try {
            // 기본 자격 증명 공급자 체인 사용
            DefaultAWSCredentialsProviderChain credentialsProvider = new DefaultAWSCredentialsProviderChain();
            
            // 자격 증명 출처 확인
            String credentialSource = "알 수 없음";
            if (System.getenv("AWS_ACCESS_KEY_ID") != null) {
                credentialSource = "환경 변수";
            } else if (System.getProperty("user.home") != null && 
                      new java.io.File(System.getProperty("user.home") + "/.aws/credentials").exists()) {
                credentialSource = "AWS 자격 증명 파일 (~/.aws/credentials)";
            }
            
            try {
                // 자격 증명 가져오기 시도
                credentialsProvider.getCredentials();
                String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
                
                // 보안상 AccessKey의 마지막 4자리만 표시
                String maskedAccessKey = "없음";
                if (accessKey != null && !accessKey.isEmpty()) {
                    maskedAccessKey = "***" + accessKey.substring(Math.max(0, accessKey.length() - 4));
                }
                
                System.out.println("- 자격 증명 출처: " + credentialSource);
                System.out.println("- AWS 리전: " + AWS_REGION.getName());
                System.out.println("- Access Key ID: " + maskedAccessKey);
                System.out.println("- Secret Key: " + (System.getenv("AWS_SECRET_ACCESS_KEY") != null ? "설정됨" : "없음"));
                
            } catch (Exception e) {
                System.out.println("- 사용 가능한 자격 증명을 찾을 수 없습니다!");
                System.out.println("- 오류: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.out.println("AWS 자격 증명 정보 확인 중 오류: " + e.getMessage());
        }
    }
} 