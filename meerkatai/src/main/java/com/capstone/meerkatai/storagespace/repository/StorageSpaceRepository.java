package com.capstone.meerkatai.storagespace.repository;

import com.capstone.meerkatai.storagespace.entity.StorageSpace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StorageSpaceRepository extends JpaRepository<StorageSpace, Integer> {
    Optional<StorageSpace> findByUserUserId(Integer userId);
}
