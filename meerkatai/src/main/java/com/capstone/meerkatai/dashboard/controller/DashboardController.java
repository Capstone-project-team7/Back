package com.capstone.meerkatai.dashboard.controller;

import com.capstone.meerkatai.dashboard.entity.Dashboard;
import com.capstone.meerkatai.dashboard.service.DashboardService;
import com.capstone.meerkatai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."))
                .getUserId();
    }

    @GetMapping("/{date}")
    public ResponseEntity<Map<String, Object>> getMonthlyDashboard(
            @PathVariable("date") String yyyyMM
    ) {
        Long userId = getCurrentUserId();  // JWT 기반 사용자 식별
        List<Map<String, Object>> result = dashboardService.getMonthlyDashboard(yyyyMM, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", result);  // ✅ 리스트가 "data" 안에 들어감

        return ResponseEntity.ok(response);
    }

}