package com.capstone.meerkatai.cctv.service;

import com.capstone.meerkatai.cctv.entity.Cctv;
import com.capstone.meerkatai.cctv.repository.CctvRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CctvService {

    private final CctvRepository cctvRepository;

    public List<Cctv> findAll() {
        return cctvRepository.findAll();
    }

    public Optional<Cctv> findById(Integer id) {
        return cctvRepository.findById(id);
    }

    public List<Cctv> findByUserId(Integer userId) {
        return cctvRepository.findByUserUserId(userId);
    }

    public Cctv save(Cctv cctv) {
        return cctvRepository.save(cctv);
    }

    public void delete(Integer id) {
        cctvRepository.deleteById(id);
    }
}
