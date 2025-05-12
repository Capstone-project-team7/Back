package com.capstone.meerkatai.config;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * 테스트 환경을 위한 AWS S3 Mock 설정
 * 
 * 실제 AWS S3 구현 대신 테스트용 Mock 객체를 사용합니다.
 */
@Configuration
@Profile("test")
public class TestAwsS3Config {

    /**
     * 테스트용 모의 S3 클라이언트
     * MockS3ServiceConfig가 없을 경우에만 등록됨
     */
    @Bean(name = "mockAmazonS3")
    @ConditionalOnMissingBean(name = "amazonS3")
    public Object mockAmazonS3() {
        // 실제 타입은 중요하지 않음 - S3Service에서 Mock 객체로 대체
        return Mockito.mock(Object.class);
    }
} 