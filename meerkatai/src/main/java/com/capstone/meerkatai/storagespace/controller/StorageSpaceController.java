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
}