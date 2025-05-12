package com.capstone.meerkatai.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.capstone.meerkatai.global.config.AwsS3Config;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {AwsS3Config.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "cloud.aws.region.static=ap-northeast-2",
    "aws.s3.bucket-name=test-bucket",
    "cloud.aws.s3.bucket=test-bucket",
    "cloud.aws.stack.auto=false"
})
public class AwsS3ConfigTest {

    @Autowired(required = false)
    private AmazonS3 amazonS3;
    
    @Autowired(required = false)
    private AmazonS3Client amazonS3Client;
    
    @Test
    void testAwsS3Beans() {
        // 빈이 등록되었는지 확인
        assertNotNull(amazonS3, "AmazonS3 bean should be registered");
        assertNotNull(amazonS3Client, "AmazonS3Client bean should be registered");
    }
} 