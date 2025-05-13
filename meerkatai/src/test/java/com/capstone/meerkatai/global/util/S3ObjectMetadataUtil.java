package com.capstone.meerkatai.global.util;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * S3 버킷 내 객체 메타데이터를 조회하는 유틸리티 클래스
 * 테스트 및 디버깅 목적으로 사용
 */
@Component
public class S3ObjectMetadataUtil {

    private final AmazonS3Client amazonS3Client;
    private final String bucketName;

    @Autowired
    public S3ObjectMetadataUtil(AmazonS3Client amazonS3Client, 
                               @Value("${cloud.aws.s3.bucket}") String bucketName) {
        this.amazonS3Client = amazonS3Client;
        this.bucketName = bucketName;
    }

    /**
     * 특정 접두사(prefix)로 시작하는 객체 목록 조회
     *
     * @param prefix 접두사 (예: "clips/", "thumbnails/")
     * @param maxKeys 최대 조회 개수
     * @return 객체 요약 정보 목록
     */
    public List<S3ObjectSummary> listObjects(String prefix, int maxKeys) {
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(prefix)
                .withMaxKeys(maxKeys);
        
        ListObjectsV2Result result = amazonS3Client.listObjectsV2(request);
        
        return result.getObjectSummaries();
    }

    /**
     * 특정 객체의 메타데이터 조회
     *
     * @param objectKey 객체 키
     * @return 객체 메타데이터
     */
    public ObjectMetadata getObjectMetadata(String objectKey) {
        return amazonS3Client.getObjectMetadata(bucketName, objectKey);
    }

    /**
     * 특정 CCTV ID와 관련된 모든 영상 파일 조회
     *
     * @param cctvId CCTV ID
     * @param maxKeys 최대 조회 개수
     * @return 영상 파일 객체 키 목록
     */
    public List<String> listVideosBycctvId(Long cctvId, int maxKeys) {
        String prefix = "clips/" + cctvId + "_";
        
        return listObjects(prefix, maxKeys).stream()
                .map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 특정 CCTV ID와 관련된 모든 썸네일 파일 조회
     *
     * @param cctvId CCTV ID
     * @param maxKeys 최대 조회 개수
     * @return 썸네일 파일 객체 키 목록
     */
    public List<String> listThumbnailsBycctvId(Long cctvId, int maxKeys) {
        String prefix = "thumbnails/" + cctvId + "_";
        
        return listObjects(prefix, maxKeys).stream()
                .map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());
    }

    /**
     * S3 버킷 내 모든 객체 개수 조회
     *
     * @return 버킷 내 객체 개수
     */
    public int countAllObjects() {
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(bucketName);
        
        ListObjectsV2Result result = amazonS3Client.listObjectsV2(request);
        return result.getKeyCount();
    }

    /**
     * 특정 접두사(prefix)로 시작하는 객체 개수 조회
     *
     * @param prefix 접두사 (예: "clips/", "thumbnails/")
     * @return 해당 접두사로 시작하는 객체 개수
     */
    public int countObjectsByPrefix(String prefix) {
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(prefix);
        
        ListObjectsV2Result result = amazonS3Client.listObjectsV2(request);
        return result.getKeyCount();
    }
} 