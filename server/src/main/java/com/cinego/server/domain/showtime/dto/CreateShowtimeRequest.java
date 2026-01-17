package com.cinego.server.domain.showtime.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShowtimeRequest {

    @NotNull(message = "Movie ID không được để trống")
    private UUID movieId;

    @NotNull(message = "Room ID không được để trống")
    private UUID roomId;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @Future(message = "Thời gian bắt đầu phải trong tương lai")
    private LocalDateTime startTime;

    @NotNull(message = "Giá vé không được để trống")
    @Positive(message = "Giá vé phải lớn hơn 0")
    private BigDecimal price;

    private String format;
}
