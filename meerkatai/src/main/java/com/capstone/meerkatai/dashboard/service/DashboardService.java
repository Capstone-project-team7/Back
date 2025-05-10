package com.capstone.meerkatai.dashboard.service;

import com.capstone.meerkatai.alarm.dto.AnomalyVideoMetadataRequest;
import com.capstone.meerkatai.dashboard.entity.Dashboard;
import com.capstone.meerkatai.dashboard.repository.DashboardRepository;
import com.capstone.meerkatai.user.entity.User;
import com.capstone.meerkatai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final UserRepository userRepository;

    public void updateDashboardWithAnomaly(AnomalyVideoMetadataRequest request) {
        Long userId = request.getUserId();
        LocalDate date = request.getTimestamp().toLocalDate();  // 날짜만 추출
        String anomalyType = request.getAnomalyType();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Optional<Dashboard> optional = dashboardRepository.findByUserAndTime(user, date);

        Dashboard dashboard = optional.orElseGet(() -> {
            Dashboard newEntry = new Dashboard();
            newEntry.setUser(user);        // ✅ User 객체 직접 설정
            newEntry.setTime(date.atStartOfDay());  // LocalDate → LocalDateTime (필드 타입 맞추기)
            newEntry.setType1Count(0);     // ✅ 기본값 초기화
            newEntry.setType2Count(0);
            newEntry.setType3Count(0);
            newEntry.setType4Count(0);
            newEntry.setType5Count(0);
            newEntry.setType6Count(0);
            newEntry.setType7Count(0);
            return newEntry;
        });


        // 2. anomalyType에 따라 해당 count 증가
        switch (anomalyType.toUpperCase()) {
            case "Fall": dashboard.setType1Count(dashboard.getType1Count() + 1); break;
            case "Damage": dashboard.setType2Count(dashboard.getType2Count() + 1); break;
            case "Fire": dashboard.setType3Count(dashboard.getType3Count() + 1); break;
            case "Smoke": dashboard.setType4Count(dashboard.getType4Count() + 1); break;
            case "Abandon": dashboard.setType5Count(dashboard.getType5Count() + 1); break;
            case "Theft": dashboard.setType6Count(dashboard.getType6Count() + 1); break;
            case "Assault": dashboard.setType7Count(dashboard.getType7Count() + 1); break;
            default: break; // 알 수 없는 유형은 무시
        }

        dashboardRepository.save(dashboard);
    }
}
