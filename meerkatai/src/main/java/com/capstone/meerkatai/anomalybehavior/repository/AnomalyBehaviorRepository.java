package com.capstone.meerkatai.anomalybehavior.repository;

import com.capstone.meerkatai.anomalybehavior.entity.AnomalyBehavior;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnomalyBehaviorRepository extends JpaRepository<AnomalyBehavior, Long> {
    // 사용자 ID로 이상행동 목록 조회
    List<AnomalyBehavior> findByUserUserId(Long userId);

    void deleteByUserUserId(Long userId);
}