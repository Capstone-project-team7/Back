package com.capstone.meerkatai.dashboard.repository;

import com.capstone.meerkatai.dashboard.entity.Dashboard;
import com.capstone.meerkatai.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DashboardRepository extends JpaRepository<Dashboard, Long> {
    List<Dashboard> findByUserUserId(Long userId);

    Optional<Dashboard> findByUserAndTime(User user, LocalDate time);
    List<Dashboard> findByUserAndTimeBetween(User user, LocalDate start, LocalDate end);

    void deleteByUserUserId(Long userId);
}