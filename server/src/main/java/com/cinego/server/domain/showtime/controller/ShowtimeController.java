package com.cinego.server.domain.showtime.controller;

import com.cinego.server.common.dto.ApiResponse;
import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.domain.showtime.dto.*;
import com.cinego.server.domain.showtime.service.ShowtimeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/showtimes")
@RequiredArgsConstructor
@Validated
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @PostMapping
    public ResponseEntity<ApiResponse<ShowtimeDTO>> createShowtime(
            @Valid @RequestBody CreateShowtimeRequest request) {
        ShowtimeDTO showtime = showtimeService.createShowtime(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo lịch chiếu thành công", showtime));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ShowtimeDTO>>> getAllShowtimes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        PageResponse<ShowtimeDTO> result = showtimeService.getAllShowtimes(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShowtimeDTO>> getShowtimeById(
            @PathVariable @NotNull(message = "Showtime ID không được để trống") UUID id) {
        ShowtimeDTO showtime = showtimeService.getShowtimeById(id);
        return ResponseEntity.ok(ApiResponse.success(showtime));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<ApiResponse<List<ShowtimeDTO>>> getShowtimesByMovieId(
            @PathVariable @NotNull(message = "Movie ID không được để trống") UUID movieId) {
        List<ShowtimeDTO> showtimes = showtimeService.getShowtimesByMovieId(movieId);
        return ResponseEntity.ok(ApiResponse.success(showtimes));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<List<ShowtimeDTO>>> getShowtimesByRoomId(
            @PathVariable @NotNull(message = "Room ID không được để trống") UUID roomId) {
        List<ShowtimeDTO> showtimes = showtimeService.getShowtimesByRoomId(roomId);
        return ResponseEntity.ok(ApiResponse.success(showtimes));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ShowtimeDTO>>> searchShowtimes(
            @RequestParam(required = false) UUID movieId,
            @RequestParam(required = false) UUID cinemaId,
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        ShowtimeSearchRequest searchRequest = ShowtimeSearchRequest.builder()
                .movieId(movieId)
                .cinemaId(cinemaId)
                .roomId(roomId)
                .startDate(startDate)
                .endDate(endDate)
                .status(status)
                .build();
        PageResponse<ShowtimeDTO> result = showtimeService.searchShowtimes(searchRequest, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ShowtimeDTO>> updateShowtime(
            @PathVariable @NotNull(message = "Showtime ID không được để trống") UUID id,
            @Valid @RequestBody UpdateShowtimeRequest request) {
        ShowtimeDTO showtime = showtimeService.updateShowtime(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật lịch chiếu thành công", showtime));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShowtime(
            @PathVariable @NotNull(message = "Showtime ID không được để trống") UUID id) {
        showtimeService.deleteShowtime(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa lịch chiếu thành công", null));
    }
}
