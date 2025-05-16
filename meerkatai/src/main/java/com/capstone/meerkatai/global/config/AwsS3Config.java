package com.capstone.meerkatai.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;

@Configuration
public class AwsS3Config {

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    /**
     * AWS S3 클라이언트 빈 등록
     * DefaultAWSCredentialsProviderChain 통해 자동으로 인증 정보 확인
     * (환경 변수, AWS CLI 설정, EC2 인스턴스 역할 등)
     */
    @Bean
    @Primary
    public AmazonS3 amazonS3() {
        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }
    
    /**
     * AmazonS3Client 빈 등록 (Spring Cloud AWS 호환용)
     */
    @Bean
    public AmazonS3Client amazonS3Client() {
        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }
} 