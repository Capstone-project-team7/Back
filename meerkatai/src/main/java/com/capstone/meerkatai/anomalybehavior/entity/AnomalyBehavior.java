package com.capstone.meerkatai.anomalybehavior.entity;


import com.capstone.meerkatai.streamingvideo.entity.StreamingVideo;
import com.capstone.meerkatai.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor
public class AnomalyBehavior {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long anomalyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnomalyBehaviorType anomalyBehaviorType;

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