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
public class Video {

    @Id
    private Integer videoId;

    @Column(nullable = false, length = 250)
    private String filePath;

    @Column(nullable = false, length = 250)
    private String thumbnailPath;

    @Column(nullable = false)
    private Integer duration;

    @Column(nullable = false)
    private Integer fileSize;

    @Column(nullable = false)
    private Boolean videoStatus;

    @ManyToOne
    @JoinColumn(name = "streaming_video_id", nullable = false)
    private StreamingVideo streamingVideo;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "anomaly_id", nullable = false, unique = true)
    private AnomalyBehavior anomalyBehavior;
}
