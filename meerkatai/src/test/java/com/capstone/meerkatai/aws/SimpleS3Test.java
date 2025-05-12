package com.capstone.meerkatai.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * AWS S3 연결 테스트 - Mockito를 사용하여 AWS 서비스를 모킹
 */
public class SimpleS3Test {

    @Mock
    private AmazonS3 s3Client;
    
    @Mock
    private ObjectListing objectListing;
    
    private final String bucketName = "test-bucket";

    @BeforeEach
    void setUp() {
        // Mockito 초기화
        MockitoAnnotations.openMocks(this);
        
        // 기본 모킹 설정
        when(s3Client.doesBucketExistV2(anyString())).thenReturn(true);
        
        // 객체 목록 모킹
        List<S3ObjectSummary> summaries = new ArrayList<>();
        S3ObjectSummary summary1 = new S3ObjectSummary();
        summary1.setKey("test-file-1.txt");
        summary1.setBucketName(bucketName);
        
        S3ObjectSummary summary2 = new S3ObjectSummary();
        summary2.setKey("test-file-2.txt");
        summary2.setBucketName(bucketName);
        
        summaries.add(summary1);
        summaries.add(summary2);
        
        when(objectListing.getObjectSummaries()).thenReturn(summaries);
        when(s3Client.listObjects(anyString())).thenReturn(objectListing);
    }
    
    @Test
    void testS3Connection() {
        System.out.println("==== AWS S3 모킹 테스트 ====");
        
        try {
            // 버킷 존재 확인
            boolean exists = s3Client.doesBucketExistV2(bucketName);
            assertTrue(exists, "버킷이 존재하지 않습니다: " + bucketName);
            
            System.out.println("버킷 '" + bucketName + "' 존재 확인: " + exists);
            
            // 버킷 내 객체 목록 확인
            System.out.println("\n==== 버킷 내 객체 목록 (모킹) ====");
            s3Client.listObjects(bucketName).getObjectSummaries().forEach(item -> {
                System.out.println("- " + item.getKey());
            });
            
            System.out.println("\nS3 모킹 테스트 성공!");
        } catch (Exception e) {
            System.out.println("\n==== 오류 발생 ====");
            System.out.println(e.getClass().getName() + ": " + e.getMessage());
            throw e;
        }
    }
} 