package com.capstone.meerkatai.storagespace.entity;

import com.capstone.meerkatai.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor
public class StorageSpace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storageId;

    @Column(nullable = false)
    private Long totalSpace;

    @Column(nullable = false)
    private Long usedSpace;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}