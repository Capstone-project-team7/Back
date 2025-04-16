package com.capstone.meerkatai.cctv.entity;

import com.capstone.meerkatai.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor
public class Cctv {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cctvId;

    //null 이어도 될것같음
    @Column(length = 20, nullable = false)
    private String cctvName;

    @Column(length = 45, nullable = false)
    private String ipAddress;

    @Column(length = 100, nullable = false)
    private String cctvAdmin;

    @Column(length = 225, nullable = false)
    private String cctvPassword;

    @Column(length = 50, nullable = false)
    private String cctvPath;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}