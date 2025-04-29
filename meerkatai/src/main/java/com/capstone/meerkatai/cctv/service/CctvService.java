package com.capstone.meerkatai.cctv.service;

import com.capstone.meerkatai.cctv.dto.CctvAddRequest;
import com.capstone.meerkatai.cctv.dto.CctvUpdateRequest;
import com.capstone.meerkatai.cctv.entity.Cctv;
import com.capstone.meerkatai.cctv.repository.CctvRepository;
import com.capstone.meerkatai.common.exception.ResourceNotFoundException;
import com.capstone.meerkatai.user.entity.User;
import com.capstone.meerkatai.user.repository.UserRepository;
import com.capstone.meerkatai.streamingvideo.entity.StreamingVideo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import com.capstone.meerkatai.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;

/**
 * CCTV 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * <p>
 * 이 클래스는 CCTV의 조회, 추가, 수정, 삭제 등의 기능을 제공합니다.
 * 기본적으로 읽기 전용 트랜잭션으로 동작하며, 데이터를 변경하는 메서드에는
 * 별도로 트랜잭션이 적용되어 있습니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CctvService {

    private static final Logger log = LoggerFactory.getLogger(CctvService.class);

    private final CctvRepository cctvRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 모든 CCTV 목록을 조회합니다.
     *
     * @return 저장된 모든 CCTV 목록
     */
    public List<Cctv> findAll() {
        return cctvRepository.findAll();
    }

    /**
     * 지정된 ID의 CCTV를 조회합니다.
     *
     * @param cctvId 조회할 CCTV의 ID
     * @return CCTV 정보를 포함한 Optional 객체, 해당 ID의 CCTV가 없으면 빈 Optional 반환
     */
    public Optional<Cctv> findById(Long cctvId) {
        return cctvRepository.findById(cctvId);
    }

    /**
     * 특정 사용자가 소유한 모든 CCTV 목록을 조회합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자가 소유한 CCTV 목록
     */
    public List<Cctv> findByUserId(Long userId) {
        return cctvRepository.findByUser_UserId(userId);
    }

    /**
     * CCTV 정보를 저장합니다.
     *
     * @param cctv 저장할 CCTV 객체
     * @return 저장된 CCTV 객체
     */
    @Transactional
    public Cctv save(Cctv cctv) {
        return cctvRepository.save(cctv);
    }

    /**
     * 새로운 CCTV를 추가합니다.
     *
     * @param request CCTV 추가에 필요한 정보를 담고 있는 요청 객체
     * @return 추가된 CCTV 객체
     * @throws ResourceNotFoundException 요청에 포함된 사용자 ID가 존재하지 않을 경우 발생
     */
    @Transactional
    public Cctv add(CctvAddRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Cctv cctv = Cctv.builder()
            .cctvName(request.getCctvName())
            .ipAddress(request.getIpAddress())
            .cctvAdmin(request.getCctvAdmin())
            .cctvPath(request.getCctvPath())
            .cctvPassword(request.getCctvPassword())
            .user(user)
            .build();

        return cctvRepository.save(cctv);
    }

    /**
     * 기존 CCTV 정보를 업데이트합니다.
     *
     * @param cctvId 업데이트할 CCTV의 ID
     * @param request 업데이트할 정보를 담고 있는 요청 객체
     * @return 업데이트된 CCTV 객체
     * @throws ResourceNotFoundException 지정된 ID의 CCTV가 존재하지 않을 경우 발생
     */
    @Transactional
    public Cctv update(Long cctvId, CctvUpdateRequest request) {
        Cctv cctv = cctvRepository.findById(cctvId)
            .orElseThrow(() -> new ResourceNotFoundException("CCTV", "id", cctvId));

        cctv.setCctvName(request.getCctvName());
        cctv.setIpAddress(request.getIpAddress());
        cctv.setCctvPath(request.getCctvPath());

        if (request.getCctvPassword() != null && !request.getCctvPassword().isEmpty()) {
            cctv.setCctvPassword(request.getCctvPassword());
        }

        return cctvRepository.save(cctv);
    }

    /**
     * 지정된 ID의 CCTV를 삭제합니다.
     * <p>
     * 이 메서드는 실제 삭제를 수행하기 전에 관련된 StreamingVideo 레코드도 함께 삭제합니다.
     * 삭제는 네이티브 SQL 쿼리를 사용하여 수행되며, 관련 레코드 삭제 순서는 다음과 같습니다:
     * 1. 먼저 해당 CCTV와 연결된 모든 StreamingVideo 레코드 삭제
     * 2. 그 다음 CCTV 레코드 삭제
     * </p>
     *
     * @param cctvId 삭제할 CCTV의 ID
     * @throws ResourceNotFoundException CCTV가 존재하지 않거나 삭제 후 확인 시 레코드가 없는 경우 발생
     * @throws BusinessException 데이터 무결성 위반 또는 기타 예외 발생 시 처리
     */
    @Transactional
    public void delete(Long cctvId) {
        if (!cctvRepository.existsById(cctvId)) {
            throw new ResourceNotFoundException("CCTV", "id", cctvId);
        }

        try {
            log.debug("Attempting to delete CCTV with id: {}", cctvId);

            // 먼저 연결된 streaming_video 레코드를 삭제
            Query deleteStreamingVideosQuery = entityManager.createNativeQuery(
                "DELETE FROM streaming_video WHERE cctv_id = :cctvId");
            deleteStreamingVideosQuery.setParameter("cctvId", cctvId);
            int deletedVideos = deleteStreamingVideosQuery.executeUpdate();
            log.debug("Deleted {} streaming videos for CCTV id {}", deletedVideos, cctvId);

            // 그 다음 CCTV 레코드 삭제
            Query deleteCctvQuery = entityManager.createNativeQuery(
                "DELETE FROM cctv WHERE cctv_id = :cctvId");
            deleteCctvQuery.setParameter("cctvId", cctvId);
            int deletedCctvs = deleteCctvQuery.executeUpdate();
            log.debug("Deleted {} CCTV records with id {}", deletedCctvs, cctvId);

            if (deletedCctvs == 0) {
                throw new ResourceNotFoundException("CCTV", "id", cctvId);
            }

            log.info("CCTV with id {} successfully deleted", cctvId);
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to delete CCTV with id {}: {}", cctvId, e.getMessage(), e);
            throw new BusinessException("이 CCTV는 다른 데이터와 연결되어 있어 삭제할 수 없습니다. 먼저 연결된 데이터를 삭제해주세요.");
        } catch (Exception e) {
            log.error("Unexpected error while deleting CCTV with id {}: {}", cctvId, e.getMessage(), e);
            throw new BusinessException("CCTV 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
