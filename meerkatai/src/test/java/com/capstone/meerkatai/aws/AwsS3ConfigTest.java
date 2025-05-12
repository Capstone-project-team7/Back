package com.capstone.meerkatai.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AWS S3 설정 테스트
 * TestAwsS3Configuration에서 생성된 빈들이 제대로 등록되는지 확인합니다.
 */
@SpringBootTest(classes = {TestAwsS3Configuration.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "cloud.aws.region.static=ap-northeast-2",
    "aws.s3.bucket-name=test-bucket",
    "cloud.aws.s3.bucket=test-bucket",
    "cloud.aws.stack.auto=false"
})
public class AwsS3ConfigTest {

    @Autowired
    private AmazonS3 amazonS3;
    
    @Autowired
    private AmazonS3Client amazonS3Client;
    
    @Test
    void testAwsS3Beans() {
        // AWS S3 빈이 정상적으로 등록되었는지 확인
        assertNotNull(amazonS3, "AmazonS3 빈이 등록되어야 합니다");
        assertNotNull(amazonS3Client, "AmazonS3Client 빈이 등록되어야 합니다");
        
        System.out.println("AmazonS3 빈: " + amazonS3.getClass().getName());
        System.out.println("AmazonS3Client 빈: " + amazonS3Client.getClass().getName());
    }
} 