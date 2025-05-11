package com.capstone.meerkatai.dashboard.service;

import com.capstone.meerkatai.alarm.dto.AnomalyVideoMetadataRequest;
import com.capstone.meerkatai.dashboard.entity.Dashboard;
import com.capstone.meerkatai.dashboard.repository.DashboardRepository;
import com.capstone.meerkatai.user.entity.User;
import com.capstone.meerkatai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final UserRepository userRepository;

    public void updateDashboardWithAnomaly(AnomalyVideoMetadataRequest request) {
        Long userId = request.getUserId();
        LocalDate date = request.getTimestamp().toLocalDate();
        String anomalyType = request.getAnomalyType();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Optional<Dashboard> optional = dashboardRepository.findByUserAndTime(user, date);

        Dashboard dashboard = optional.orElseGet(() -> {
            Dashboard newEntry = new Dashboard();
            newEntry.setUser(user);
            newEntry.setTime(date);  // LocalDate 그대로 저장
            newEntry.setType1Count(0);
            newEntry.setType2Count(0);
            newEntry.setType3Count(0);
            newEntry.setType4Count(0);
            newEntry.setType5Count(0);
            newEntry.setType6Count(0);
            newEntry.setType7Count(0);
            return newEntry;
        });

        switch (anomalyType.toUpperCase()) {
            case "FALL" -> dashboard.setType1Count(dashboard.getType1Count() + 1);
            case "DAMAGE" -> dashboard.setType2Count(dashboard.getType2Count() + 1);
            case "FIRE" -> dashboard.setType3Count(dashboard.getType3Count() + 1);
            case "SMOKE" -> dashboard.setType4Count(dashboard.getType4Count() + 1);
            case "ABANDON" -> dashboard.setType5Count(dashboard.getType5Count() + 1);
            case "THEFT" -> dashboard.setType6Count(dashboard.getType6Count() + 1);
            case "ASSAULT" -> dashboard.setType7Count(dashboard.getType7Count() + 1);
        }

        dashboardRepository.save(dashboard);
    }

    public List<Map<String, Object>> getMonthlyDashboard(String yyyyMM, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // yyyy-MM 문자열을 LocalDate로 파싱
        YearMonth yearMonth = YearMonth.parse(yyyyMM); // java.time.YearMonth
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // DashboardRepository에서 LocalDate 기반 조회
        List<Dashboard> dashboards = dashboardRepository.findByUserAndTimeBetween(user, startDate, endDate);

        List<Map<String, Object>> result = new ArrayList<>();

        for (Dashboard dash : dashboards) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", dash.getTime().toString()); // LocalDate → "YYYY-MM-DD"

            if (dash.getType1Count() > 0) entry.put("type1Count", dash.getType1Count());
            if (dash.getType2Count() > 0) entry.put("type2Count", dash.getType2Count());
            if (dash.getType3Count() > 0) entry.put("type3Count", dash.getType3Count());
            if (dash.getType4Count() > 0) entry.put("type4Count", dash.getType4Count());
            if (dash.getType5Count() > 0) entry.put("type5Count", dash.getType5Count());
            if (dash.getType6Count() > 0) entry.put("type6Count", dash.getType6Count());
            if (dash.getType7Count() > 0) entry.put("type7Count", dash.getType7Count());

            if (entry.size() > 1) result.add(entry); // date 외 카운트가 있는 경우만 추가
        }

        return result;
    }

}
