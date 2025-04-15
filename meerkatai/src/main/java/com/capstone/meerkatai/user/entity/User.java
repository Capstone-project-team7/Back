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

//사용자 정보를 저장하는 엔티티 클래스
@Entity
@Table(name = "user")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "user_password", nullable = false)
    private String password;

    @Column(name = "user_name", length = 20, nullable = false)
    private String name;

    @Column(name = "notify_status", nullable = false)
    private boolean notification;
    @Column(name = "agreement_status", nullable = false)
    private boolean agreement;

    /**
     * 사용자의 최초 로그인 여부
     * 최초 가입 시 true로 설정, 첫 로그인 후 false로 변경됩니다.
     */
    @Column(name = "first_login", nullable = false)
    private boolean firstLogin;

    /**
     * 사용자의 역할
     * USER: 일반 사용자, ADMIN: 관리자
     * @Enumerated(EnumType.STRING)
     * private Role role;
     */


    private LocalDateTime lastLoginAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    //비밀번호를 암호화하는 메서드

    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }

    public void updateUserInfo(String name, boolean notification) {
        this.name = name;
        this.notification = notification;
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
        this.firstLogin = false;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 사용자의 권한 정보를 반환하는 메서드
     @Override
     public Collection<? extends GrantedAuthority> getAuthorities() {
     return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
     }
     */


    @Override
    public String getUsername() {
        return email;
    }


