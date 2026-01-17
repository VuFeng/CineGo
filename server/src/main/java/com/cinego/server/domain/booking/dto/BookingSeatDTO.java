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
public class BookingSeatDTO {
    private UUID id;
    private UUID seatId;
    private String seatRow;
    private Integer seatNumber;
    private String seatType;
    private String status;
    private BigDecimal price;
}
