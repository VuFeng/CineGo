package com.cinego.server.domain.showtime.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShowtimeRequest {

    private LocalDateTime startTime;

    private BigDecimal price;

    private String status;

    private String format;
}
