package com.cinego.server.domain.payment.controller;

import com.cinego.server.common.dto.ApiResponse;
import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.domain.payment.dto.CreatePaymentRequest;
import com.cinego.server.domain.payment.dto.PaymentDTO;
import com.cinego.server.domain.payment.dto.UpdatePaymentRequest;
import com.cinego.server.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentDTO>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentDTO payment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo thanh toán thành công", payment));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PaymentDTO>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        PageResponse<PaymentDTO> result = paymentService.getAllPayments(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentById(
            @PathVariable @NotNull(message = "Payment ID không được để trống") UUID id) {
        PaymentDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentByTransactionId(
            @PathVariable @NotBlank(message = "Transaction ID không được để trống") String transactionId) {
        PaymentDTO payment = paymentService.getPaymentByTransactionId(transactionId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getPaymentsByBookingId(
            @PathVariable @NotNull(message = "Booking ID không được để trống") UUID bookingId) {
        List<PaymentDTO> payments = paymentService.getPaymentsByBookingId(bookingId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentDTO>> updatePayment(
            @PathVariable @NotNull(message = "Payment ID không được để trống") UUID id,
            @Valid @RequestBody UpdatePaymentRequest request) {
        PaymentDTO payment = paymentService.updatePayment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thanh toán thành công", payment));
    }

    @PostMapping("/{id}/simulate-success")
    public ResponseEntity<ApiResponse<PaymentDTO>> simulatePaymentSuccess(
            @PathVariable @NotNull(message = "Payment ID không được để trống") UUID id) {
        PaymentDTO payment = paymentService.simulatePaymentSuccess(id);
        return ResponseEntity.ok(ApiResponse.success("Mô phỏng thanh toán thành công", payment));
    }
}
