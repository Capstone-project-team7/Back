package com.capstone.meerkatai.storagespace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageSpaceResponse {
    private Long storage_id;
    private Long total_space;
    private Long used_space;
    private Long available_space;
    private Double usage_percentage;
    private Long user_id;
}
