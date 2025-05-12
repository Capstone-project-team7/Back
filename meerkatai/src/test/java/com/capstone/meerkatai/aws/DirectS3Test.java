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
        System.out.println("환경 변수 ACCESS_KEY가 설정되었나요? " + (accessKey != null && !accessKey.isEmpty()));
        System.out.println("환경 변수 SECRET_KEY가 설정되었나요? " + (secretKey != null && !secretKey.isEmpty()));
        
        // 로컬 환경에서 AWS 자격 증명 파일 확인
        String userHome = System.getProperty("user.home");
        String awsCredentialsPath = userHome + "/.aws/credentials";
        boolean awsCredentialsExists = new java.io.File(awsCredentialsPath).exists();
        System.out.println("AWS 자격 증명 파일이 존재하나요? " + awsCredentialsExists);
        
        // AWS 자격 증명 파일이 있으면 테스트 진행 가능
        boolean canProceed = (accessKey != null && !accessKey.isEmpty() && 
                             secretKey != null && !secretKey.isEmpty()) || 
                             awsCredentialsExists;
        
        // 테스트를 위해 하드코딩된 키 사용 (실제 운영 환경에서는 사용하지 말 것!)
        // 아래 주석을 해제하여 테스트하고, 테스트 후에는 반드시 다시 주석 처리할 것
        // accessKey = "YOUR_ACCESS_KEY_HERE";
        // secretKey = "YOUR_SECRET_KEY_HERE";
        // canProceed = true;
        
        if (!canProceed) {
            System.out.println("AWS 자격 증명이 설정되지 않았습니다. 테스트를 건너뜁니다.");
            System.out.println("환경 변수 또는 ~/.aws/credentials 파일에 자격 증명을 설정하세요.");
            return;
        }
        
        try {
            // 기본적인 S3 클라이언트 생성
            AmazonS3 s3Client;
            
            if (accessKey != null && !accessKey.isEmpty() && secretKey != null && !secretKey.isEmpty()) {
                // 환경 변수 자격 증명 사용
                BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
                s3Client = AmazonS3ClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(credentials))
                        .withRegion(AWS_REGION)
                        .build();
                System.out.println("환경 변수에서 자격 증명을 가져왔습니다.");
            } else {
                // 기본 자격 증명 공급자 체인 사용 (AWS 자격 증명 파일 등)
                s3Client = AmazonS3ClientBuilder.standard()
                        .withRegion(AWS_REGION)
                        .build();
                System.out.println("AWS 자격 증명 파일에서 자격 증명을 가져왔습니다.");
            }
            
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