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

    @GetMapping
    public List<Dashboard> getAll() {
        return dashboardService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Dashboard> getById(@PathVariable Integer id) {
        return dashboardService.findById(id);
    }

    @GetMapping("/user/{userId}")
    public List<Dashboard> getByUserId(@PathVariable Integer userId) {
        return dashboardService.findByUserId(userId);
    }

    @PostMapping
    public Dashboard create(@RequestBody Dashboard dashboard) {
        return dashboardService.save(dashboard);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        dashboardService.delete(id);
    }
}
