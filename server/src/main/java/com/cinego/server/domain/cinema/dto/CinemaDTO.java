package com.cinego.server.domain.cinema.dto;

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
public class CinemaDTO {
    private UUID id;
    private String name;
    private String address;
    private String city;
    private String district;
    private String phone;
    private String email;
    private String openingHours;
    private String imageUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

