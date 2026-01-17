package com.cinego.server.domain.cinema.service;

import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.common.exception.ConflictException;
import com.cinego.server.common.exception.ResourceNotFoundException;
import com.cinego.server.common.util.PageUtil;
import com.cinego.server.domain.cinema.dto.CinemaDTO;
import com.cinego.server.domain.cinema.dto.CreateCinemaRequest;
import com.cinego.server.domain.cinema.dto.UpdateCinemaRequest;
import com.cinego.server.domain.cinema.entity.Cinema;
import com.cinego.server.domain.cinema.mapper.CinemaMapper;
import com.cinego.server.domain.cinema.repository.CinemaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final CinemaMapper cinemaMapper;

    @Transactional
    public CinemaDTO createCinema(CreateCinemaRequest request) {
        log.info("Creating cinema with name: {}", request.getName());

        Cinema cinema = new Cinema();
        cinema.setName(request.getName());
        cinema.setAddress(request.getAddress());
        cinema.setCity(request.getCity());
        cinema.setDistrict(request.getDistrict());
        cinema.setPhone(request.getPhone());
        cinema.setEmail(request.getEmail());
        cinema.setOpeningHours(request.getOpeningHours());
        cinema.setImageUrl(request.getImageUrl());
        cinema.setIsActive(true);

        // Set createdAt/updatedAt thủ công vì @CreationTimestamp không hoạt động ổn với Supabase pooler
        LocalDateTime now = LocalDateTime.now();
        cinema.setCreatedAt(now);
        cinema.setUpdatedAt(now);

        Cinema saved = cinemaRepository.save(cinema);
        return cinemaMapper.toDTO(saved);
    }

    public CinemaDTO getCinemaById(UUID id) {
        log.info("Getting cinema by id: {}", id);
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema", "id", id));
        return cinemaMapper.toDTO(cinema);
    }

    public PageResponse<CinemaDTO> getCinemas(String city, String district, int page, int size) {
        Pageable pageable = PageUtil.createPageable(page, size, "createdAt", "ASC");

        Page<Cinema> result;
        if (city != null && !city.isBlank() && district != null && !district.isBlank()) {
            result = cinemaRepository.findByCityIgnoreCaseAndDistrictIgnoreCase(city, district, pageable);
        } else if (city != null && !city.isBlank()) {
            result = cinemaRepository.findByCityIgnoreCase(city, pageable);
        } else {
            result = cinemaRepository.findAll(pageable);
        }

        return PageUtil.toPageResponse(result.map(cinemaMapper::toDTO));
    }

    @Transactional
    public CinemaDTO updateCinema(UUID id, UpdateCinemaRequest request) {
        log.info("Updating cinema with id: {}", id);
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema", "id", id));

        if (request.getName() != null && !request.getName().isBlank()) {
            cinema.setName(request.getName());
        }
        if (request.getAddress() != null) {
            cinema.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            cinema.setCity(request.getCity());
        }
        if (request.getDistrict() != null) {
            cinema.setDistrict(request.getDistrict());
        }
        if (request.getPhone() != null) {
            cinema.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            cinema.setEmail(request.getEmail());
        }
        if (request.getOpeningHours() != null) {
            cinema.setOpeningHours(request.getOpeningHours());
        }
        if (request.getImageUrl() != null) {
            cinema.setImageUrl(request.getImageUrl());
        }
        if (request.getIsActive() != null) {
            cinema.setIsActive(request.getIsActive());
        }

        cinema.setUpdatedAt(LocalDateTime.now());

        Cinema updated = cinemaRepository.save(cinema);
        return cinemaMapper.toDTO(updated);
    }

    @Transactional
    public void deleteCinema(UUID id) {
        log.info("Deleting cinema with id: {}", id);
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema", "id", id));

        // Kiểm tra xem có room nào không
        if (!cinema.getRooms().isEmpty()) {
            throw new ConflictException("Không thể xóa rạp đang có phòng chiếu");
        }

        cinemaRepository.delete(cinema);
        cinemaRepository.flush();
        log.info("Cinema deleted successfully with id: {}", id);
    }
}

