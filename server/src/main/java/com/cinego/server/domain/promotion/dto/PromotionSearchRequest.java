package com.cinego.server.domain.promotion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionSearchRequest {
    private String code;
    private Boolean isActive;
    private UUID movieId; // Tìm promotion áp dụng cho movie này
}
