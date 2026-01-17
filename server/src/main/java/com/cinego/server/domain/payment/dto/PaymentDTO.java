package com.cinego.server.domain.payment.dto;

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
public class PaymentDTO {
    private UUID id;
    private UUID bookingId;
    private String bookingCode;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentProvider;
    private String transactionId;
    private String status;
    private String paymentUrl;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private String refundReason;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
