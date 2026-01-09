package com.cinego.server.domain.room.dto;

import com.cinego.server.domain.room.entity.Room.RoomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {

    @NotBlank(message = "Tên phòng không được để trống")
    private String name;

    @Min(value = 1, message = "Tổng số ghế phải lớn hơn 0")
    private Integer totalSeats;

    private String seatLayout;

    private RoomType roomType;
}

