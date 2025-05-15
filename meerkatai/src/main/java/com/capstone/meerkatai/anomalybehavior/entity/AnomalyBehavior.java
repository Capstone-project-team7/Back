package com.capstone.meerkatai.anomalybehavior.entity;


import com.capstone.meerkatai.streamingvideo.entity.StreamingVideo;
import com.capstone.meerkatai.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "anomaly_behavior")
public class AnomalyBehavior {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "anomaly_id", nullable = false, updatable = false)
    private Long anomalyId;

    @Column(nullable = false)
    private String anomalyBehaviorType;

    @Column(nullable = false)
    private LocalDateTime anomalyTime;

    @Column(nullable = false, length = 250)
    private String anomalyVideoLink;

    @Column(nullable = false, length = 250)
    private String anomalyThumbnailLink;

    @ManyToOne
    @JoinColumn(name = "streaming_video_id", nullable = false)
    private StreamingVideo streamingVideo;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}