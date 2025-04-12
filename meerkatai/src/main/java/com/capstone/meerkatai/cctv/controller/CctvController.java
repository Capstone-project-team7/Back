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

    @GetMapping
    public List<Cctv> getAll() {
        return cctvService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Cctv> getById(@PathVariable Integer id) {
        return cctvService.findById(id);
    }

    @GetMapping("/user/{userId}")
    public List<Cctv> getByUserId(@PathVariable Integer userId) {
        return cctvService.findByUserId(userId);
    }

    @PostMapping
    public Cctv create(@RequestBody Cctv cctv) {
        return cctvService.save(cctv);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        cctvService.delete(id);
    }
}
