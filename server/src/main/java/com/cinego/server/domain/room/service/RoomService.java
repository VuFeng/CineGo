package com.cinego.server.domain.room.service;

import com.cinego.server.common.exception.ResourceNotFoundException;
import com.cinego.server.domain.cinema.entity.Cinema;
import com.cinego.server.domain.cinema.repository.CinemaRepository;
import com.cinego.server.domain.room.dto.CreateRoomRequest;
import com.cinego.server.domain.room.dto.RoomDTO;
import com.cinego.server.domain.room.dto.UpdateRoomRequest;
import com.cinego.server.domain.room.entity.Room;
import com.cinego.server.domain.room.mapper.RoomMapper;
import com.cinego.server.domain.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final CinemaRepository cinemaRepository;
    private final RoomMapper roomMapper;

    @Transactional
    public RoomDTO createRoom(UUID cinemaId, CreateRoomRequest request) {
        log.info("Creating room for cinema: {}", cinemaId);
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema", "id", cinemaId));

        Room room = new Room();
        room.setCinema(cinema);
        room.setName(request.getName());
        room.setTotalSeats(request.getTotalSeats());
        room.setSeatLayout(request.getSeatLayout());
        room.setRoomType(request.getRoomType());
        room.setIsActive(true);

        // Set createdAt/updatedAt thủ công vì @CreationTimestamp không hoạt động ổn với Supabase pooler
        LocalDateTime now = LocalDateTime.now();
        room.setCreatedAt(now);
        room.setUpdatedAt(now);

        Room saved = roomRepository.save(room);
        return roomMapper.toDTO(saved);
    }

    public RoomDTO getRoomById(UUID id) {
        log.info("Getting room by id: {}", id);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
        return roomMapper.toDTO(room);
    }

    public List<RoomDTO> getRoomsByCinema(UUID cinemaId) {
        log.info("Getting rooms for cinema: {}", cinemaId);
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema", "id", cinemaId));

        List<Room> rooms = roomRepository.findByCinema(cinema);
        return rooms.stream()
                .map(roomMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomDTO updateRoom(UUID id, UpdateRoomRequest request) {
        log.info("Updating room with id: {}", id);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));

        if (request.getName() != null && !request.getName().isBlank()) {
            room.setName(request.getName());
        }
        if (request.getTotalSeats() != null) {
            room.setTotalSeats(request.getTotalSeats());
        }
        if (request.getSeatLayout() != null) {
            room.setSeatLayout(request.getSeatLayout());
        }
        if (request.getRoomType() != null) {
            room.setRoomType(request.getRoomType());
        }
        if (request.getIsActive() != null) {
            room.setIsActive(request.getIsActive());
        }

        room.setUpdatedAt(LocalDateTime.now());

        Room updated = roomRepository.save(room);
        return roomMapper.toDTO(updated);
    }
}

