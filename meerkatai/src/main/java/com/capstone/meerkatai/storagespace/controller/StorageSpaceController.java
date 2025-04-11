package com.capstone.meerkatai.storagespace.controller;

import com.capstone.meerkatai.storagespace.entity.StorageSpace;
import com.capstone.meerkatai.storagespace.service.StorageSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageSpaceController {

    private final StorageSpaceService storageSpaceService;

    @GetMapping("/{id}")
    public Optional<StorageSpace> getById(@PathVariable Integer id) {
        return storageSpaceService.findById(id);
    }

    @GetMapping("/user/{userId}")
    public Optional<StorageSpace> getByUserId(@PathVariable Integer userId) {
        return storageSpaceService.findByUserId(userId);
    }

    @PostMapping
    public StorageSpace create(@RequestBody StorageSpace storageSpace) {
        return storageSpaceService.save(storageSpace);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        storageSpaceService.delete(id);
    }
}
