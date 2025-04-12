package com.capstone.meerkatai.anomalybehavior.controller;

import com.capstone.meerkatai.anomalybehavior.entity.AnomalyBehavior;
import com.capstone.meerkatai.anomalybehavior.service.AnomalyBehaviorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/anomalies")
@RequiredArgsConstructor
public class AnomalyBehaviorController {

    private final AnomalyBehaviorService anomalyBehaviorService;

    @GetMapping
    public List<AnomalyBehavior> getAll() {
        return anomalyBehaviorService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<AnomalyBehavior> getById(@PathVariable Integer id) {
        return anomalyBehaviorService.findById(id);
    }

    @PostMapping
    public AnomalyBehavior create(@RequestBody AnomalyBehavior behavior) {
        return anomalyBehaviorService.save(behavior);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        anomalyBehaviorService.delete(id);
    }
}
