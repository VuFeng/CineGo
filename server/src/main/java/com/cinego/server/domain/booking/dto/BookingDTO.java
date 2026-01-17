package com.cinego.server.domain.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private String userName;
    private UUID showtimeId;
    private String movieTitle;
    private LocalDateTime showtimeStartTime;
    private String cinemaName;
    private String roomName;
    private String status;
    private String paymentStatus;
    private String paymentMethod;
    private BigDecimal totalPrice;
    private String bookingCode;
    private String qrCodeUrl;
    private List<BookingSeatDTO> seats;
    private List<BookingPromotionDTO> promotions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
