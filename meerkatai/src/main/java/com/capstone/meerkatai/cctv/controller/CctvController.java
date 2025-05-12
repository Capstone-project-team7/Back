package com.capstone.meerkatai.cctv.controller;

import com.capstone.meerkatai.cctv.dto.*;
import com.capstone.meerkatai.cctv.entity.Cctv;
import com.capstone.meerkatai.cctv.service.CctvService;
import com.capstone.meerkatai.common.dto.ApiResponse;
import com.capstone.meerkatai.common.exception.ResourceNotFoundException;
import com.capstone.meerkatai.streamingvideo.entity.StreamingVideo;
import com.capstone.meerkatai.streamingvideo.repository.StreamingVideoRepository;
import com.capstone.meerkatai.user.entity.User;
import com.capstone.meerkatai.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;


/**
 * CCTV 관련 API 요청을 처리하는 REST 컨트롤러입니다.
 * <p>
 * 이 컨트롤러는 CCTV 목록 조회, 상세 정보 조회, 추가, 수정, 삭제 등의
 * CCTV 관련 요청을 처리합니다. 모든 응답은 ApiResponse 객체로 래핑되어
 * 일관된 형식으로 클라이언트에게 전달됩니다.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/cctv")
@RequiredArgsConstructor
public class CctvController {

    private final CctvService cctvService;
    private final UserRepository userRepository;
    private final StreamingVideoRepository streamingVideoRepository;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."))
                .getUserId();
    }

    /**
     * 현재 인증된 사용자의 CCTV 목록을 조회합니다.
     * JWT 토큰에서 추출한 사용자 정보를 사용하여 해당 사용자의 CCTV만 반환합니다.
     *
     * @return 현재 인증된 사용자의 CCTV 정보가 포함된 ApiResponse 객체
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, List<CctvResponse>>> getCctvList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        List<Cctv> cctvs = cctvService.findByUserId(user.getUserId());

        List<CctvResponse> responses = cctvs.stream()
                .map(cctv -> {
                    // ✅ 해당 CCTV에 대한 스트리밍 상태 확인
                    Boolean isActive = streamingVideoRepository
                            .findByUserUserIdAndCctvCctvId(user.getUserId(), cctv.getCctvId())
                            .map(StreamingVideo::getStreamingVideoStatus)
                            .orElse(false); // 없으면 false

                    return CctvResponse.builder()
                            .cctvId(cctv.getCctvId())
                            .cctvName(cctv.getCctvName())
                            .ipAddress(cctv.getIpAddress())
                            .createdAt(cctv.getCreatedAt())
                            .updatedAt(cctv.getUpdatedAt())
                            .is_active(isActive)
                            .build();
                })
                .collect(Collectors.toList());

        Map<String, List<CctvResponse>> data = new HashMap<>();
        data.put("cctvs", responses);

        return ApiResponse.success(data);
    }


    /**
     * 특정 CCTV의 상세 정보를 조회합니다.
     * 요청한 CCTV가 현재 인증된 사용자의 것인지 확인합니다.
     *
     * @param cctvId 조회할 CCTV의 고유 식별자
     * @return CCTV 상세 정보가 포함된 ApiResponse 객체
     * @throws ResourceNotFoundException 해당 ID의 CCTV가 존재하지 않을 경우 발생
     */
    @GetMapping("/info/{cctvId}")
    public ApiResponse<CctvDetailResponse> getCctvInfo(@PathVariable Long cctvId) {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 이메일로 사용자 ID 조회
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // CCTV 정보 조회
        Cctv cctv = cctvService.findById(cctvId)
            .orElseThrow(() -> new ResourceNotFoundException("CCTV", "id", cctvId));

        // 요청한 CCTV가 현재 사용자의 것인지 확인
        if (!cctv.getUser().getUserId().equals(user.getUserId())) {
            throw new ResourceNotFoundException("CCTV", "id", cctvId);
        }

        CctvDetailResponse response = convertToCctvDetailResponse(cctv);
        return ApiResponse.success(response);
    }

    /**
     * 새로운 CCTV를 추가합니다.
     * CCTV 추가 요청에 포함된 사용자 ID가 현재 인증된 사용자와 일치하는지 확인합니다.
     *
     * @param request CCTV 추가에 필요한 정보를 담고 있는 요청 객체
     * @return 추가된 CCTV 정보가 포함된 ApiResponse 객체
     */
    @PostMapping("/add")
    public ApiResponse<CctvResponse> addCctv(@RequestBody CctvAddRequest request) {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 이메일로 사용자 ID 조회
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // 요청의 사용자 ID를 현재 인증된 사용자의 ID로 설정
        request = CctvAddRequest.builder()
            .cctvName(request.getCctvName())
            .ipAddress(request.getIpAddress())
            .cctvAdmin(request.getCctvAdmin())
            .cctvPath(request.getCctvPath())
            .cctvPassword(request.getCctvPassword())
            .userId(user.getUserId())
            .build();

        Cctv cctv = cctvService.add(request);
        CctvResponse response = convertToCctvResponse(cctv);
        return ApiResponse.success(response);
    }

    /**
     * 기존 CCTV 정보를 수정합니다.
     * 요청한 CCTV가 현재 인증된 사용자의 것인지 확인합니다.
     *
     * @param cctvId 수정할 CCTV의 고유 식별자
     * @param request CCTV 수정에 필요한 정보를 담고 있는 요청 객체
     * @return 수정 결과가 포함된 ApiResponse 객체
     * @throws ResourceNotFoundException 해당 ID의 CCTV가 존재하지 않을 경우 발생
     */
    @PutMapping("/update/{cctvId}")
    public ApiResponse<Map<String, Object>> updateCctv(
        @PathVariable Long cctvId,
        @RequestBody CctvUpdateRequest request) {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 이메일로 사용자 ID 조회
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // CCTV 정보 조회
        Cctv cctv = cctvService.findById(cctvId)
            .orElseThrow(() -> new ResourceNotFoundException("CCTV", "id", cctvId));

        // 요청한 CCTV가 현재 사용자의 것인지 확인
        if (!cctv.getUser().getUserId().equals(user.getUserId())) {
            throw new ResourceNotFoundException("CCTV", "id", cctvId);
        }

        cctv = cctvService.update(cctvId, request);

        Map<String, Object> data = new HashMap<>();
        data.put("cctv_id", cctv.getCctvId());
        data.put("cctv_name", cctv.getCctvName());
        data.put("updated_at", cctv.getUpdatedAt());
        data.put("updated", true);

        return ApiResponse.success(data);
    }

    /**
     * 특정 CCTV를 삭제합니다.
     * 요청한 CCTV가 현재 인증된 사용자의 것인지 확인합니다.
     *
     /* @param cctvId 삭제할 CCTV의 고유 식별자
     * @return 삭제 결과가 포함된 ApiResponse 객체
     * @throws ResourceNotFoundException 해당 ID의 CCTV가 존재하지 않을 경우 발생
     * @throws Exception 삭제 과정에서 다른 오류가 발생할 경우
     */
    @DeleteMapping("/delete")
    public ApiResponse<List<Map<String, Object>>> deleteCctvs(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CctvDeleteRequest request
    ) {
        // 현재 사용자 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Long cctvId : request.getCctvIds()) {
            Map<String, Object> result = new HashMap<>();
            result.put("cctv_id", cctvId);

            try {
                Cctv cctv = cctvService.findById(cctvId)
                        .orElseThrow(() -> new ResourceNotFoundException("CCTV", "id", cctvId));

                if (!cctv.getUser().getUserId().equals(user.getUserId())) {
                    throw new ResourceNotFoundException("CCTV", "id", cctvId);
                }

                cctvService.delete(cctvId);
                result.put("deleted", true);
            } catch (Exception e) {
                result.put("deleted", false);
                result.put("error", e.getMessage());
            }

            resultList.add(result);
        }

        return ApiResponse.success(resultList);
    }


    /**
     * Cctv 엔티티를 CctvResponse DTO로 변환합니다.
     *
     * @param cctv 변환할 Cctv 엔티티
     * @return 변환된 CctvResponse 객체
     */
    private CctvResponse convertToCctvResponse(Cctv cctv) {
        return CctvResponse.builder()
            .cctvId(cctv.getCctvId())
            .cctvName(cctv.getCctvName())
            .ipAddress(cctv.getIpAddress())
            .createdAt(cctv.getCreatedAt())
            .updatedAt(cctv.getUpdatedAt())
            .build();
    }

    /**
     * Cctv 엔티티를 CctvDetailResponse DTO로 변환합니다.
     *
     * @param cctv 변환할 Cctv 엔티티
     * @return 변환된 CctvDetailResponse 객체
     */
    private CctvDetailResponse convertToCctvDetailResponse(Cctv cctv) {
        return CctvDetailResponse.builder()
            .cctvId(cctv.getCctvId())
            .cctvName(cctv.getCctvName())
            .ipAddress(cctv.getIpAddress())
            .cctvAdmin(cctv.getCctvAdmin())
            .cctvPath(cctv.getCctvPath())
            .createdAt(cctv.getCreatedAt())
            .updatedAt(cctv.getUpdatedAt())
            .userId(cctv.getUser().getUserId())
            .build();
    }
}
