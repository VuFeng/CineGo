package com.cinego.server.domain.room.controller;

import com.cinego.server.common.dto.ApiResponse;
import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.domain.room.dto.CreateRoomRequest;
import com.cinego.server.domain.room.dto.RoomDTO;
import com.cinego.server.domain.room.dto.UpdateRoomRequest;
import com.cinego.server.domain.room.service.RoomService;
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
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/cinemas/{cinemaId}/rooms")
    public ResponseEntity<ApiResponse<RoomDTO>> createRoom(
            @PathVariable @NotNull(message = "Cinema ID không được để trống") UUID cinemaId,
            @Valid @RequestBody CreateRoomRequest request) {
        RoomDTO room = roomService.createRoom(cinemaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo phòng thành công", room));
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<PageResponse<RoomDTO>>> getAllRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        PageResponse<RoomDTO> result = roomService.getAllRooms(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/cinemas/{cinemaId}/rooms")
    public ResponseEntity<ApiResponse<List<RoomDTO>>> getRoomsByCinema(
            @PathVariable @NotNull(message = "Cinema ID không được để trống") UUID cinemaId) {
        List<RoomDTO> rooms = roomService.getRoomsByCinema(cinemaId);
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<ApiResponse<RoomDTO>> getRoomById(
            @PathVariable @NotNull(message = "Room ID không được để trống") UUID id) {
        RoomDTO room = roomService.getRoomById(id);
        return ResponseEntity.ok(ApiResponse.success(room));
    }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<ApiResponse<RoomDTO>> updateRoom(
            @PathVariable @NotNull(message = "Room ID không được để trống") UUID id,
            @Valid @RequestBody UpdateRoomRequest request) {
        RoomDTO room = roomService.updateRoom(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật phòng thành công", room));
    }
}

