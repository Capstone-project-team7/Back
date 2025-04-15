package com.capstone.meerkatai.user.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.meerkatai.user.entity.User;
import com.capstone.meerkatai.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 정보를 관리하는 서비스 클래스입니다.
 *
 * @see com.capstone.meerkatai.user.entity.User
 * @see com.capstone.meerkatai.user.repository.UserRepository
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * ID로 특정 사용자를 조회하는 메서드입니다.
     */
    public Optional<User> findById(Integer userId) {
        return userRepository.findById(userId);
    }

    /**
     * 이메일로 특정 사용자를 조회하는 메서드입니다.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 사용자를 저장하는 메서드입니다.
     * 새로운 사용자 생성과 기존 사용자 정보 수정에 모두 사용됩니다.
     */
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * 사용자를 삭제하는 메서드입니다.
     */
    @Transactional
    public void deleteById(Integer userId) { userRepository.deleteById(userId);
    }
}
