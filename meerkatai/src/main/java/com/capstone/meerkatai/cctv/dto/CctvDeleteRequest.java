package com.capstone.meerkatai.cctv.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// CctvDeleteRequest.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CctvDeleteRequest {
    private List<Long> cctvIds;
}

