package com.cinego.server.domain.seat.service;

import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.common.exception.ConflictException;
import com.cinego.server.common.exception.ResourceNotFoundException;
import com.cinego.server.common.util.PageUtil;
import com.cinego.server.domain.room.entity.Room;
import com.cinego.server.domain.room.repository.RoomRepository;
import com.cinego.server.domain.seat.dto.BulkCreateSeatsRequest;
import com.cinego.server.domain.seat.dto.CreateSeatRequest;
import com.cinego.server.domain.seat.dto.SeatDTO;
import com.cinego.server.domain.seat.entity.Seat;
import com.cinego.server.domain.seat.mapper.SeatMapper;
import com.cinego.server.domain.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;
    private final RoomRepository roomRepository;
    private final SeatMapper seatMapper;

    @Transactional
    public SeatDTO createSeat(UUID roomId, CreateSeatRequest request) {
        log.info("Creating seat in room: {}, row: {}, number: {}", roomId, request.getRow(), request.getNumber());
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));

        // Check duplicate seat
        boolean exists = seatRepository.existsByRoomIdAndRowAndNumber(roomId, request.getRow(), request.getNumber());
        log.debug("Checking duplicate seat - roomId={}, row={}, number={}, exists={}", roomId, request.getRow(), request.getNumber(), exists);
        if (exists) {
            log.warn("Duplicate seat detected: room={}, row={}, number={}", roomId, request.getRow(), request.getNumber());
            throw new ConflictException(
                    String.format("Ghế %s%d đã tồn tại trong phòng này", request.getRow(), request.getNumber())
            );
        }

        Seat seat = new Seat();
        seat.setRoom(room);
        seat.setRow(request.getRow());
        seat.setNumber(request.getNumber());
        seat.setSeatType(request.getSeatType());
        seat.setIsActive(true);

        // Set createdAt/updatedAt thủ công
        LocalDateTime now = LocalDateTime.now();
        seat.setCreatedAt(now);
        seat.setUpdatedAt(now);

        Seat saved = seatRepository.save(seat);
        seatRepository.flush();
        return seatMapper.toDTO(saved);
    }

    @Transactional
    public List<SeatDTO> bulkCreateSeats(UUID roomId, BulkCreateSeatsRequest request) {
        log.info("Bulk creating seats in room: {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));

        char startRow = request.getStartRow().charAt(0);
        char endRow = request.getEndRow().charAt(0);

        // Check duplicates trước khi tạo
        List<String> duplicateSeats = new ArrayList<>();
        for (char row = startRow; row <= endRow; row++) {
            for (int number = request.getStartNumber(); number <= request.getEndNumber(); number++) {
                if (seatRepository.existsByRoomIdAndRowAndNumber(roomId, String.valueOf(row), number)) {
                    duplicateSeats.add(String.format("%s%d", row, number));
                }
            }
        }

        if (!duplicateSeats.isEmpty()) {
            throw new ConflictException(
                    String.format("Các ghế sau đã tồn tại: %s", String.join(", ", duplicateSeats))
            );
        }

        // Tạo các ghế mới
        List<Seat> seats = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (char row = startRow; row <= endRow; row++) {
            for (int number = request.getStartNumber(); number <= request.getEndNumber(); number++) {
                Seat seat = new Seat();
                seat.setRoom(room);
                seat.setRow(String.valueOf(row));
                seat.setNumber(number);
                seat.setSeatType(request.getSeatType());
                seat.setIsActive(true);
                seat.setCreatedAt(now);
                seat.setUpdatedAt(now);
                seats.add(seat);
            }
        }

        List<Seat> saved = seatRepository.saveAll(seats);
        seatRepository.flush();
        return saved.stream()
                .map(seatMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<SeatDTO> getSeatsByRoom(UUID roomId) {
        log.info("Getting seats for room: {}", roomId);
        // Verify room exists
        roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));

        List<Seat> seats = seatRepository.findByRoomId(roomId);
        return seats.stream()
                .map(seatMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SeatDTO updateSeat(UUID id, Boolean isActive) {
        log.info("Updating seat with id: {}", id);
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", "id", id));

        if (isActive != null) {
            seat.setIsActive(isActive);
        }

        seat.setUpdatedAt(LocalDateTime.now());

        Seat updated = seatRepository.save(seat);
        return seatMapper.toDTO(updated);
    }

    @Transactional(readOnly = true)
    public PageResponse<SeatDTO> getAllSeats(int page, int size, String sortBy, String sortDirection) {
        log.info("Getting all seats with pagination - page: {}, size: {}", page, size);
        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<Seat> seatPage = seatRepository.findAll(pageable);
        return PageUtil.toPageResponse(seatPage.map(seatMapper::toDTO));
    }
}

