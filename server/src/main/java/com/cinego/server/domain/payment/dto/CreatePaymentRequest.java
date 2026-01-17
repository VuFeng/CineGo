package com.cinego.server.domain.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    @NotNull(message = "Booking ID không được để trống")
    private UUID bookingId;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod; // CASH, CARD, MOMO, ZALOPAY

    private String paymentProvider; // VNPAY, MOMO, ZALOPAY, CASH
}
