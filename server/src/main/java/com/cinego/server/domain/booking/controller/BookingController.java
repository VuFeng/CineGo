package com.cinego.server.domain.booking.controller;

import com.cinego.server.common.dto.ApiResponse;
import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.domain.booking.dto.BookingDTO;
import com.cinego.server.domain.booking.dto.CreateBookingRequest;
import com.cinego.server.domain.booking.dto.UpdateBookingRequest;
import com.cinego.server.domain.booking.service.BookingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingDTO>> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {
        BookingDTO booking = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đặt vé thành công", booking));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookingDTO>>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        PageResponse<BookingDTO> result = bookingService.getAllBookings(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<ApiResponse<PageResponse<BookingDTO>>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        PageResponse<BookingDTO> result = bookingService.getMyBookings(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingDTO>> getBookingById(
            @PathVariable @NotNull(message = "Booking ID không được để trống") UUID id) {
        BookingDTO booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    @GetMapping("/code/{bookingCode}")
    public ResponseEntity<ApiResponse<BookingDTO>> getBookingByCode(
            @PathVariable @NotBlank(message = "Booking code không được để trống") String bookingCode) {
        BookingDTO booking = bookingService.getBookingByCode(bookingCode);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingDTO>> updateBooking(
            @PathVariable @NotNull(message = "Booking ID không được để trống") UUID id,
            @Valid @RequestBody UpdateBookingRequest request) {
        BookingDTO booking = bookingService.updateBooking(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật booking thành công", booking));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @PathVariable @NotNull(message = "Booking ID không được để trống") UUID id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Hủy đặt vé thành công", null));
    }
}
