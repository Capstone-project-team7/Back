package com.capstone.meerkatai.cctv.entity;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import com.capstone.meerkatai.streamingvideo.entity.StreamingVideo;
import com.capstone.meerkatai.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * CCTV 정보를 저장하는 엔티티 클래스입니다.
 * <p>
 * 이 클래스는 CCTV의 기본 정보(이름, IP 주소, 관리자, 경로, 비밀번호 등)를 저장하며,
 * User 엔티티와 다대일(N:1) 관계를 맺고 있습니다. 또한 StreamingVideo 엔티티와는
 * 일대다(1:N) 관계를 맺어 하나의 CCTV가 여러 스트리밍 비디오를 가질 수 있습니다.
 * </p>
 */
@Entity
@Table(name = "CCTV")
@Getter @Setter @NoArgsConstructor
@Builder
@AllArgsConstructor
public class Cctv {

    /**
     * CCTV의 고유 식별자입니다.
     * 자동 생성되는 증가 값(AUTO INCREMENT)으로 설정되어 있습니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cctv_id")
    private Long cctvId;

    /**
     * CCTV의 이름입니다.
     * 최대 20자까지 입력 가능하며, NULL 값을 허용하지 않습니다.
     */
    @Column(name = "cctv_name", nullable = false, length = 20)
    private String cctvName;

    /**
     * CCTV의 IP 주소입니다.
     * 최대 45자까지 입력 가능하며, NULL 값을 허용하지 않습니다.
     */
    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    /**
     * CCTV의 관리자 정보입니다.
     * 최대 50자까지 입력 가능하며, NULL 값을 허용하지 않습니다.
     */
    @Column(name = "cctv_admin", nullable = false, length = 50)
    private String cctvAdmin;

    /**
     * CCTV의 접근 경로입니다.
     * 최대 50자까지 입력 가능하며, NULL 값을 허용하지 않습니다.
     */
    @Column(name = "cctv_path", nullable = false, length = 50)
    private String cctvPath;

    /**
     * CCTV 접속에 필요한 비밀번호입니다.
     * 최대 225자까지 입력 가능하며, NULL 값을 허용하지 않습니다.
     */
    @Column(name = "cctv_password", nullable = false, length = 225)
    private String cctvPassword;

    /**
     * CCTV 정보가 생성된 시간입니다.
     * 생성 후에는 변경할 수 없습니다.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * CCTV 정보가 마지막으로 업데이트된 시간입니다.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * CCTV를 소유한 사용자 정보입니다.
     * User 엔티티와 다대일(N:1) 관계로 연결됩니다.
     * LAZY 로딩을 사용하여 필요할 때만 데이터를 로드합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * CCTV와 연결된 스트리밍 비디오 목록입니다.
     * StreamingVideo 엔티티와 일대다(1:N) 관계로 연결됩니다.
     * CCTV가 삭제될 때 연결된 모든 스트리밍 비디오도 함께 삭제됩니다(cascade = CascadeType.ALL).
     * 부모에서 제거된 자식 엔티티는 삭제됩니다(orphanRemoval = true).
     */
    @OneToMany(mappedBy = "cctv", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StreamingVideo> streamingVideos = new ArrayList<>();

    /**
     * CCTV 엔티티가 처음 생성될 때 호출되는 메서드입니다.
     * 생성 시간과 업데이트 시간을 현재 시간으로 설정합니다.
     */
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * CCTV 엔티티가 업데이트될 때 호출되는 메서드입니다.
     * 업데이트 시간을 현재 시간으로 변경합니다.
     */
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
