package com.capstone.meerkatai.dashboard.controller;

import com.capstone.meerkatai.dashboard.entity.Dashboard;
import com.capstone.meerkatai.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dashboards")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

}