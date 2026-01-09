package com.cinego.server.domain.seat.dto;

import com.cinego.server.domain.seat.entity.Seat.SeatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDTO {
    private UUID id;
    private UUID roomId;
    private String row;
    private Integer number;
    private SeatType seatType;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

