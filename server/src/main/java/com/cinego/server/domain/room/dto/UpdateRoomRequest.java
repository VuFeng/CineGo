package com.cinego.server.domain.room.dto;

import com.cinego.server.domain.room.entity.Room.RoomType;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoomRequest {

    private String name;

    @Min(value = 1, message = "Tổng số ghế phải lớn hơn 0")
    private Integer totalSeats;

    private String seatLayout;

    private RoomType roomType;

    private Boolean isActive;
}

