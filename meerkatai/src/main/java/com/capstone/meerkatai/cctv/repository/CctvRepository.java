package com.capstone.meerkatai.cctv.repository;

import com.capstone.meerkatai.cctv.entity.Cctv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CctvRepository extends JpaRepository<Cctv, Long> {
}
