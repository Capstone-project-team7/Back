package com.capstone.meerkatai.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 테스트 환경에서 사용할 모의 AWS S3 설정
 * Mockito를 사용하여 실제 AWS 서비스 호출을 모의 응답으로 대체합니다.
 */
@Configuration
@Profile("test")
public class MockAwsConfiguration {

    // 모의 버킷에 저장된 객체를 시뮬레이션하기 위한 메모리 저장소
    private final Map<String, byte[]> mockObjects = new HashMap<>();

    /**
     * 모의 AmazonS3 클라이언트를 생성합니다.
     */
    @Bean
    @Primary
    public AmazonS3 amazonS3() {
        AmazonS3 mockS3 = Mockito.mock(AmazonS3.class);
        
        // 버킷 존재 여부 확인 메소드 모킹
        Mockito.when(mockS3.doesBucketExistV2(Mockito.anyString())).thenReturn(true);
        
        // 파일 존재 여부 확인 메소드 모킹
        Mockito.when(mockS3.doesObjectExist(Mockito.anyString(), Mockito.anyString())).thenAnswer(invocation -> {
            String bucket = invocation.getArgument(0);
            String key = invocation.getArgument(1);
            return mockObjects.containsKey(bucket + "/" + key);
        });
        
        // 객체 업로드 메소드 모킹
        Mockito.when(mockS3.putObject(Mockito.anyString(), Mockito.anyString(), Mockito.any(InputStream.class), Mockito.any())).thenAnswer(invocation -> {
            String bucket = invocation.getArgument(0);
            String key = invocation.getArgument(1);
            InputStream inputStream = invocation.getArgument(2);
            
            // 입력 스트림 데이터를 바이트 배열에 저장
            byte[] data = inputStream.readAllBytes();
            mockObjects.put(bucket + "/" + key, data);
            
            return new PutObjectResult();
        });
        
        // 객체 조회 메소드 모킹
        Mockito.when(mockS3.getObject(Mockito.anyString(), Mockito.anyString())).thenAnswer(invocation -> {
            String bucket = invocation.getArgument(0);
            String key = invocation.getArgument(1);
            String objectKey = bucket + "/" + key;
            
            if (!mockObjects.containsKey(objectKey)) {
                throw new RuntimeException("Object not found");
            }
            
            S3Object s3Object = new S3Object();
            s3Object.setObjectContent(new S3ObjectInputStream(
                    new ByteArrayInputStream(mockObjects.get(objectKey)),
                    null));
            return s3Object;
        });
        
        // 객체 삭제 메소드 모킹
        Mockito.doAnswer(invocation -> {
            String bucket = invocation.getArgument(0);
            String key = invocation.getArgument(1);
            mockObjects.remove(bucket + "/" + key);
            return null;
        }).when(mockS3).deleteObject(Mockito.anyString(), Mockito.anyString());
        
        // URL 생성 메소드 모킹
        Mockito.when(mockS3.generatePresignedUrl(Mockito.anyString(), Mockito.anyString(), Mockito.any(Date.class))).thenAnswer(invocation -> {
            String bucket = invocation.getArgument(0);
            String key = invocation.getArgument(1);
            return new URL("https://" + bucket + ".s3.amazonaws.com/" + key);
        });
        
        return mockS3;
    }
    
    /**
     * 모의 AmazonS3Client를 생성합니다.
     */
    @Bean
    @Primary
    public AmazonS3Client amazonS3Client() {
        AmazonS3Client mockS3Client = Mockito.mock(AmazonS3Client.class);
        
        // 버킷 존재 여부 확인 메소드 모킹
        Mockito.when(mockS3Client.doesBucketExistV2(Mockito.anyString())).thenReturn(true);
        
        // Presigned URL 생성 메소드 모킹
        Mockito.when(mockS3Client.generatePresignedUrl(Mockito.any())).thenAnswer(invocation -> {
            return new URL("https://test-bucket.s3.amazonaws.com/test-file-" + System.currentTimeMillis());
        });
        
        return mockS3Client;
    }
} 