package com.capstone.meerkatai.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.meerkatai.user.entity.User;
import com.capstone.meerkatai.user.entity.Role;

 // 사용자 엔티티에 접근하기 위한 JPA 레포지토리 인터페이스


public interface UserRepository extends JpaRepository<User, Integer> {

    //이메일로 사용자를 조회하는 메서드
    Optional<User> findByEmail(String email);

    /**
     * 이메일이 이미 존재하는지 확인하는 메서드
     *
     * @param email 확인할 이메일
     * @return 이메일이 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByEmail(String email);
}
