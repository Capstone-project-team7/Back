package com.capstone.meerkatai.dashboard.service;

import com.capstone.meerkatai.dashboard.entity.Dashboard;
import com.capstone.meerkatai.dashboard.repository.DashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public List<Dashboard> findAll() {
        return dashboardRepository.findAll();
    }

    public Optional<Dashboard> findById(Integer id) {
        return dashboardRepository.findById(id);
    }

    public List<Dashboard> findByUserId(Integer userId) {
        return dashboardRepository.findByUserUserId(userId);
    }

    public Dashboard save(Dashboard dashboard) {
        return dashboardRepository.save(dashboard);
    }

    public void delete(Integer id) {
        dashboardRepository.deleteById(id);
    }
}
