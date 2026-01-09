package com.cinego.server.domain.cinema.controller;

import com.cinego.server.common.dto.ApiResponse;
import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.domain.cinema.dto.CinemaDTO;
import com.cinego.server.domain.cinema.dto.CreateCinemaRequest;
import com.cinego.server.domain.cinema.dto.UpdateCinemaRequest;
import com.cinego.server.domain.cinema.service.CinemaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cinemas")
@RequiredArgsConstructor
@Validated
public class CinemaController {

    private final CinemaService cinemaService;

    @PostMapping
    public ResponseEntity<ApiResponse<CinemaDTO>> createCinema(
            @Valid @RequestBody CreateCinemaRequest request) {
        CinemaDTO cinema = cinemaService.createCinema(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo rạp thành công", cinema));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CinemaDTO>> getCinemaById(
            @PathVariable @NotNull(message = "Cinema ID không được để trống") UUID id) {
        CinemaDTO cinema = cinemaService.getCinemaById(id);
        return ResponseEntity.ok(ApiResponse.success(cinema));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CinemaDTO>>> getCinemas(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<CinemaDTO> result = cinemaService.getCinemas(city, district, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CinemaDTO>> updateCinema(
            @PathVariable @NotNull(message = "Cinema ID không được để trống") UUID id,
            @Valid @RequestBody UpdateCinemaRequest request) {
        CinemaDTO cinema = cinemaService.updateCinema(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật rạp thành công", cinema));
    }
}

