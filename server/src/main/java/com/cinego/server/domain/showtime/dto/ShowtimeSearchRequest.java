package com.cinego.server.domain.showtime.dto;

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
public class ShowtimeSearchRequest {
    private UUID movieId;
    private UUID cinemaId;
    private UUID roomId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
}
