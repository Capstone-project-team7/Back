package com.capstone.meerkatai.cctv.controller;

import com.capstone.meerkatai.cctv.entity.Cctv;
import com.capstone.meerkatai.cctv.service.CctvService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cctvs")
@RequiredArgsConstructor
public class CctvController {

    private final CctvService cctvService;

}
