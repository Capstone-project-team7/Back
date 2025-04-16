package com.capstone.meerkatai.user.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 정보를 저장하는 엔티티 클래스입니다.
 * <p>
 * 이 클래스는 사용자의 계정 정보, 개인 정보, 권한 정보 등을 관리합니다.
 * Spring Security의 UserDetails 인터페이스를 구현하여 인증 시스템과 통합됩니다.
 * JPA Auditing 기능을 통해 생성 시간과 수정 시간을 자동으로 관리합니다.
 */
@Entity
@Table(name = "user")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {

    /**
     * 사용자의 고유 식별자입니다.
     * 자동 증가 전략을 사용합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    /**
     * 사용자의 이메일 주소입니다.
     * 고유한 값이어야 하며, 로그인 ID로 사용됩니다.
     */
    @Column(name = "user_email", length = 100, nullable = false, unique = true)
    private String email;

    /**
     * 사용자의 암호화된 비밀번호입니다.
     */
    @Column(name = "user_password", nullable = false)
    private String password;

    /**
     * 사용자의 이름입니다.
     */
    @Column(name = "user_name", length = 20, nullable = false)
    private String name;

    /**
     * 사용자의 알림 설정 상태입니다.
     * true: 알림 활성화, false: 알림 비활성화
     */
    @Column(name = "notify_status", nullable = false)
    private boolean notification;

    /**
     * 사용자의 서비스 이용 약관 동의 상태입니다.
     * true: 동의, false: 미동의
     */
    @Column(name = "agreement_status", nullable = false)
    private boolean agreement;

    /**
     * 사용자의 최초 로그인 여부입니다.
     * 최초 가입 시 true로 설정되며, 첫 로그인 후 false로 변경됩니다.
     */
    @Column(name = "first_login", nullable = false)
    private boolean firstLogin;



    /**
     * 사용자의 역할입니다.
     * USER: 일반 사용자, ADMIN: 관리자
     * 문자열로 저장됩니다.
     */
    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     * 사용자의 마지막 로그인 시간입니다.
     */
    private LocalDateTime lastLoginAt;

    /**
     * 사용자 계정이 생성된 시간입니다.
     * 생성 후 변경되지 않습니다.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 사용자 정보가 마지막으로 업데이트된 시간입니다.
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 비밀번호를 암호화하는 메서드입니다.
     *
     * @param passwordEncoder 사용할 비밀번호 암호화 객체
     */
    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }

    /**
     * 사용자 정보를 업데이트하는 메서드입니다.
     *
     * @param name 변경할 사용자 이름
     * @param notification 변경할 알림 설정 상태
     */
    public void updateUserInfo(String name, boolean notification) {
        this.name = name;
        this.notification = notification;
    }

    /**
     * 로그인 시간을 업데이트하는 메서드입니다.
     * 최초 로그인 여부를 false로 설정합니다.
     */
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
        this.firstLogin = false;
    }

    /**
     * 엔티티 생성 시 자동으로 호출되는 메서드입니다.
     * 생성 시간과 수정 시간을 초기화합니다.
     */
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * 엔티티 업데이트 시 자동으로 호출되는 메서드입니다.
     * 수정 시간을 현재 시간으로 업데이트합니다.
     */
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 사용자의 권한 정보를 반환하는 메서드입니다.
     * 역할에 기반한 권한이 부여됩니다.
     *
     * @return 사용자의 권한 컬렉션
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * 사용자의 로그인 ID를 반환하는 메서드입니다.
     * 이 애플리케이션에서는 이메일을 로그인 ID로 사용합니다.
     *
     * @return 사용자의 이메일
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * 계정 만료 여부를 반환하는 메서드입니다.
     * 이 애플리케이션에서는 계정 만료 기능을 사용하지 않습니다.
     *
     * @return 항상 true (만료되지 않음)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠금 여부를 반환하는 메서드입니다.
     * 이 애플리케이션에서는 계정 잠금 기능을 사용하지 않습니다.
     *
     * @return 항상 true (잠기지 않음)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 자격 증명(비밀번호) 만료 여부를 반환하는 메서드입니다.
     * 이 애플리케이션에서는 자격 증명 만료 기능을 사용하지 않습니다.
     *
     * @return 항상 true (만료되지 않음)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정 활성화 여부를 반환하는 메서드입니다.
     * 이 애플리케이션에서는 모든 계정이 활성화되어 있습니다.
     *
     * @return 항상 true (활성화됨)
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}