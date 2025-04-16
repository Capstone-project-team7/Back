package com.capstone.meerkatai.anomalybehavior.repository;

import com.capstone.meerkatai.anomalybehavior.entity.AnomalyBehavior;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnomalyBehaviorRepository extends JpaRepository<AnomalyBehavior, Long> {
}
