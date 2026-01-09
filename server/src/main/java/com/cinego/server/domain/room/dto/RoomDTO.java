package com.cinego.server.domain.room.dto;

import com.cinego.server.domain.room.entity.Room.RoomType;
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
public class RoomDTO {
    private UUID id;
    private UUID cinemaId;
    private String name;
    private Integer totalSeats;
    private String seatLayout;
    private RoomType roomType;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

