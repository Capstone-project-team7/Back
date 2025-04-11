package com.capstone.meerkatai.storagespace.service;

import com.capstone.meerkatai.storagespace.entity.StorageSpace;
import com.capstone.meerkatai.storagespace.repository.StorageSpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StorageSpaceService {

    private final StorageSpaceRepository storageSpaceRepository;

    public Optional<StorageSpace> findById(Integer id) {
        return storageSpaceRepository.findById(id);
    }

    public Optional<StorageSpace> findByUserId(Integer userId) {
        return storageSpaceRepository.findByUserUserId(userId);
    }

    public StorageSpace save(StorageSpace storageSpace) {
        return storageSpaceRepository.save(storageSpace);
    }

    public void delete(Integer id) {
        storageSpaceRepository.deleteById(id);
    }
}
