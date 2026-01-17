package com.cinego.server.domain.showtime.dto;

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
public class ShowtimeDTO {
    private UUID id;
    private UUID movieId;
    private String movieTitle;
    private Integer movieDuration;
    private UUID roomId;
    private String roomName;
    private UUID cinemaId;
    private String cinemaName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal price;
    private String status;
    private Integer availableSeats;
    private String format;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
