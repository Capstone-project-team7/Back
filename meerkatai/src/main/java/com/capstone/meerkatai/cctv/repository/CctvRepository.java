package com.capstone.meerkatai.cctv.repository;

import com.capstone.meerkatai.cctv.entity.Cctv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * CCTV 엔티티에 대한 데이터 액세스 계층(Repository)입니다.
 * <p>
 * 이 인터페이스는 JpaRepository를 확장하여 CCTV 엔티티에 대한 CRUD 작업 및
 * 기본적인 데이터 액세스 기능을 제공합니다. 또한 사용자 ID를 기반으로 CCTV를
 * 조회하는 추가 메서드를 정의합니다.
 * </p>
 */
public interface CctvRepository extends JpaRepository<Cctv, Long> {
    /**
     * 지정된 사용자 ID에 속하는 모든 CCTV를 조회합니다.
     * <p>
     * 이 메서드는 Spring Data JPA의 쿼리 메서드 명명 규칙을 따릅니다.
     * '_'를 사용하여 엔티티 간의 관계를 탐색합니다(Cctv.user.id).
     * </p>
     *
     * @param userId 조회할 사용자의 ID
     * @return 사용자에게 속한 CCTV 목록
     */
    //List<Cctv> findByUser_Id(Long Id);
    List<Cctv> findByUser_UserId(Long userId);
}
