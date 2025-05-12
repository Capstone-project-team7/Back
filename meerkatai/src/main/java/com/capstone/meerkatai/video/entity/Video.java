package com.capstone.meerkatai.video.entity;

import com.capstone.meerkatai.anomalybehavior.entity.AnomalyBehavior;
import com.capstone.meerkatai.streamingvideo.entity.StreamingVideo;
import com.capstone.meerkatai.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long videoId;

    @Column(nullable = false, length = 250)
    private String filePath;

    @Column(nullable = false, length = 250)
    private String thumbnailPath;

    @Column(nullable = false)
    private Long duration;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private Boolean videoStatus;

    @ManyToOne
    @JoinColumn(name = "streaming_video_id", nullable = false)
    private StreamingVideo streamingVideo;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "anomaly_id", nullable = false, unique = true)
    private AnomalyBehavior anomalyBehavior;
}