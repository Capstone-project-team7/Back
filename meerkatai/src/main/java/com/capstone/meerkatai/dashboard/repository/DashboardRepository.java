package com.capstone.meerkatai.dashboard.repository;

import com.capstone.meerkatai.dashboard.entity.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DashboardRepository extends JpaRepository<Dashboard, Integer> {
    List<Dashboard> findByUserUserId(Integer userId);
}
