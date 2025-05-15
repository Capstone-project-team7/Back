package com.capstone.meerkatai.storagespace.controller;

import com.capstone.meerkatai.storagespace.dto.StorageSpaceResponse;
import com.capstone.meerkatai.storagespace.entity.StorageSpace;
import com.capstone.meerkatai.storagespace.repository.StorageSpaceRepository;
import com.capstone.meerkatai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class StorageSpaceController {

    private final StorageSpaceRepository storageSpaceRepository;
    private final UserRepository userRepository;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."))
                .getUserId();
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getStorageInfo() {
        Long userId = getCurrentUserId();

        StorageSpace space = storageSpaceRepository.findByUserUserId(userId)
                .orElseThrow(() -> new RuntimeException("저장공간 정보 없음"));

        Long total = space.getTotalSpace();
        Long used = space.getUsedSpace();
        Long available = total - used;
        double percent = total > 0 ? (used * 100.0 / total) : 0.0;

        StorageSpaceResponse response = StorageSpaceResponse.builder()
                .storage_id(space.getStorageId())
                .total_space(total)
                .used_space(used)
                .available_space(available)
                .usage_percentage(Math.round(percent * 10) / 10.0)
                .user_id(userId)
                .build();

        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("data", response);

        return ResponseEntity.ok(result);
    }
}