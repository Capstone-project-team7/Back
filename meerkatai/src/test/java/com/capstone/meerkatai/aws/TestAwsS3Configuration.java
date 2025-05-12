package com.capstone.meerkatai.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * 통합 테스트 환경에서 사용할 AWS S3 설정 클래스
 */
@Configuration
@Profile("test")
public class TestAwsS3Configuration {

    @Value("${cloud.aws.region.static:ap-northeast-2}")
    private String region;
    
    @Value("${cloud.aws.s3.bucket:cctv-recordings-yuhan-20250505}")
    private String bucket;

    /**
     * 테스트용 AmazonS3 빈 생성
     */
    @Bean
    @Primary
    public AmazonS3 amazonS3() {
        // 시스템 환경 변수에서 직접 가져옴
        String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
        String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        
        if (accessKey == null || secretKey == null || accessKey.isEmpty() || secretKey.isEmpty()) {
            // 테스트 환경에서는 더미 자격 증명 사용
            accessKey = "test-access-key";
            secretKey = "test-secret-key";
        }
        
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.fromName(region))
                .build();
    }

    /**
     * 테스트용 AmazonS3Client 빈 생성
     */
    @Bean
    @Primary
    public AmazonS3Client amazonS3Client() {
        // 시스템 환경 변수에서 직접 가져옴
        String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
        String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        
        if (accessKey == null || secretKey == null || accessKey.isEmpty() || secretKey.isEmpty()) {
            // 테스트 환경에서는 더미 자격 증명 사용
            accessKey = "test-access-key";
            secretKey = "test-secret-key";
        }
        
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.fromName(region))
                .build();
    }
} 