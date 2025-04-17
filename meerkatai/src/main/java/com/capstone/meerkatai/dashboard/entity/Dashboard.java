package com.capstone.meerkatai.dashboard.entity;

import com.capstone.meerkatai.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor
public class Dashboard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dataId;

    @Column(nullable = false)
    private LocalDateTime time;

    @Column(nullable = false)
    private Integer type1Count;
    @Column(nullable = false)
    private Integer type2Count;
    @Column(nullable = false)
    private Integer type3Count;
    @Column(nullable = false)
    private Integer type4Count;
    @Column(nullable = false)
    private Integer type5Count;
    @Column(nullable = false)
    private Integer type6Count;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}