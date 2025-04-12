package com.capstone.meerkatai.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor
public class User {
    @Id
    private Integer userId;

    @Column(nullable = false, length = 100)
    private String userEmail;

    @Column(nullable = false, length = 255)
    private String userPassword;

    @Column(nullable = false, length = 20)
    private String userName;

    @Column(nullable = false)
    private Boolean notifyStatus;

    @Column(nullable = false)
    private Boolean agreementStatus;

    @Column(nullable = false)
    private Boolean firstLogin;
}