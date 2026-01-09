package com.cinego.server.domain.seat.mapper;

import com.cinego.server.domain.seat.dto.SeatDTO;
import com.cinego.server.domain.seat.entity.Seat;
import org.springframework.stereotype.Component;

@Component
public class SeatMapper {

    public SeatDTO toDTO(Seat seat) {
        if (seat == null) {
            return null;
        }

        return SeatDTO.builder()
                .id(seat.getId())
                .roomId(seat.getRoom() != null ? seat.getRoom().getId() : null)
                .row(seat.getRow())
                .number(seat.getNumber())
                .seatType(seat.getSeatType())
                .isActive(seat.getIsActive())
                .createdAt(seat.getCreatedAt())
                .updatedAt(seat.getUpdatedAt())
                .build();
    }
}

