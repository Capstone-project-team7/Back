package com.capstone.meerkatai.streamingvideo.entity;

import com.capstone.meerkatai.cctv.entity.Cctv;
import com.capstone.meerkatai.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor
public class StreamingVideo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long streamingVideoId;

    @Column(nullable = false)
    private Boolean streamingVideoStatus;

    @ManyToOne
    @JoinColumn(name = "cctv_id", nullable = false)
    private Cctv cctv;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}