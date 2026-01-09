package com.cinego.server.domain.seat.dto;

import com.cinego.server.domain.seat.entity.Seat.SeatType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkCreateSeatsRequest {

    @NotNull(message = "Hàng ghế bắt đầu không được để trống")
    private String startRow;

    @NotNull(message = "Hàng ghế kết thúc không được để trống")
    private String endRow;

    @NotNull(message = "Số ghế bắt đầu không được để trống")
    @Min(value = 1, message = "Số ghế bắt đầu phải lớn hơn 0")
    private Integer startNumber;

    @NotNull(message = "Số ghế kết thúc không được để trống")
    @Min(value = 1, message = "Số ghế kết thúc phải lớn hơn 0")
    private Integer endNumber;

    private SeatType seatType;
}

