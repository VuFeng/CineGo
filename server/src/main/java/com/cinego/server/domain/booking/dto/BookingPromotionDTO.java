package com.cinego.server.domain.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingPromotionDTO {
    private UUID id;
    private UUID promotionId;
    private String promotionCode;
    private String promotionName;
    private BigDecimal discountAmount;
}
