package com.cinego.server.domain.seat.controller;

import com.cinego.server.common.dto.ApiResponse;
import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.domain.seat.dto.BulkCreateSeatsRequest;
import com.cinego.server.domain.seat.dto.CreateSeatRequest;
import com.cinego.server.domain.seat.dto.SeatDTO;
import com.cinego.server.domain.seat.service.SeatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
public class SeatController {

    private final SeatService seatService;

    @PostMapping("/rooms/{roomId}/seats")
    public ResponseEntity<ApiResponse<SeatDTO>> createSeat(
            @PathVariable @NotNull(message = "Room ID không được để trống") UUID roomId,
            @Valid @RequestBody CreateSeatRequest request) {
        SeatDTO seat = seatService.createSeat(roomId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo ghế thành công", seat));
    }

    @PostMapping("/rooms/{roomId}/seats/bulk")
    public ResponseEntity<ApiResponse<List<SeatDTO>>> bulkCreateSeats(
            @PathVariable @NotNull(message = "Room ID không được để trống") UUID roomId,
            @Valid @RequestBody BulkCreateSeatsRequest request) {
        List<SeatDTO> seats = seatService.bulkCreateSeats(roomId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo nhiều ghế thành công", seats));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SeatDTO>>> getAllSeats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        PageResponse<SeatDTO> result = seatService.getAllSeats(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/rooms/{roomId}/seats")
    public ResponseEntity<ApiResponse<List<SeatDTO>>> getSeatsByRoom(
            @PathVariable @NotNull(message = "Room ID không được để trống") UUID roomId) {
        List<SeatDTO> seats = seatService.getSeatsByRoom(roomId);
        return ResponseEntity.ok(ApiResponse.success(seats));
    }
}

