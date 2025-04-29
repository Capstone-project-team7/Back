package com.capstone.meerkatai.anomalybehavior.service;

import com.capstone.meerkatai.anomalybehavior.entity.AnomalyBehavior;
import com.capstone.meerkatai.anomalybehavior.repository.AnomalyBehaviorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnomalyBehaviorService {

    private final AnomalyBehaviorRepository anomalyBehaviorRepository;
}