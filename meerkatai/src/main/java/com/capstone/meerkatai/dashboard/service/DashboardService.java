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

}
