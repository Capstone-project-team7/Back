package com.capstone.meerkatai.streamingvideo.entity;

import com.capstone.meerkatai.anomalybehavior.entity.AnomalyBehavior;
import com.capstone.meerkatai.cctv.entity.Cctv;
import com.capstone.meerkatai.user.entity.User;
import com.capstone.meerkatai.video.entity.Video;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 스트리밍 비디오 정보를 저장하는 엔티티 클래스입니다.
 * <p>
 * 사용자(User)와 CCTV(Cctv) 엔티티와 다대일(N:1) 관계를 맺고 있으며,
 * 특정 CCTV의 스트리밍 영상에 대한 메타데이터(시작 시간, 종료 시간, URL 등)를 저장합니다.
 * </p>
 */
@Entity
@Getter @Setter @NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreamingVideo {
    /**
     * 스트리밍 비디오의 고유 식별자입니다.
     * 자동 생성되는 증가 값(AUTO INCREMENT)으로 설정되어 있습니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long streamingVideoId;

    /**
     * 스트리밍 비디오를 소유한 사용자 정보입니다.
     * User 엔티티와 다대일(N:1) 관계로 연결됩니다.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 스트리밍 비디오가 녹화된 CCTV 정보입니다.
     * Cctv 엔티티와 다대일(N:1) 관계로 연결됩니다.
     */
    @ManyToOne
    @JoinColumn(name = "cctv_id")
    private Cctv cctv;

    /**
     * 스트리밍 비디오의 시작 시간입니다.
     */
    private LocalDateTime startTime;

    /**
     * 스트리밍 비디오의 종료 시간입니다.
     */
    private LocalDateTime endTime;

    /**
     * 스트리밍 비디오에 접근할 수 있는 URL입니다.
     */
    private String streamingUrl;

    private Boolean streamingVideoStatus;

    @Builder.Default
    @OneToMany(mappedBy = "streamingVideo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Video> videos = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "streamingVideo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnomalyBehavior> anomalyBehaviors = new ArrayList<>();
}