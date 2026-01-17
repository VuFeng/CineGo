package com.cinego.server.domain.showtime.service;

import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.common.exception.BadRequestException;
import com.cinego.server.common.exception.ConflictException;
import com.cinego.server.common.exception.ResourceNotFoundException;
import com.cinego.server.common.util.PageUtil;
import com.cinego.server.domain.movie.entity.Movie;
import com.cinego.server.domain.movie.repository.MovieRepository;
import com.cinego.server.domain.room.entity.Room;
import com.cinego.server.domain.room.repository.RoomRepository;
import com.cinego.server.domain.showtime.dto.*;
import com.cinego.server.domain.showtime.entity.Showtime;
import com.cinego.server.domain.showtime.mapper.ShowtimeMapper;
import com.cinego.server.domain.showtime.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final ShowtimeMapper showtimeMapper;

    public ShowtimeDTO createShowtime(CreateShowtimeRequest request) {
        log.info("Creating showtime for movie: {} in room: {}", request.getMovieId(), request.getRoomId());

        // Load movie và room
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", request.getMovieId()));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", request.getRoomId()));

        // Kiểm tra room có active không
        if (!room.getIsActive()) {
            throw new BadRequestException("Phòng chiếu không hoạt động");
        }

        // Tính toán endTime từ startTime và movie duration
        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration());

        // Kiểm tra conflict với showtime khác trong cùng room
        List<Showtime> conflictingShowtimes = showtimeRepository.findByRoomIdAndTimeRange(
                request.getRoomId(), startTime, endTime);

        if (!conflictingShowtimes.isEmpty()) {
            throw new ConflictException("Phòng chiếu đã có lịch chiếu trong khoảng thời gian này");
        }

        // Tạo showtime mới
        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(startTime);
        showtime.setEndTime(endTime);
        showtime.setPrice(request.getPrice());
        showtime.setStatus(Showtime.ShowtimeStatus.ACTIVE);
        showtime.setAvailableSeats(room.getTotalSeats());
        showtime.setFormat(request.getFormat());

        LocalDateTime now = LocalDateTime.now();
        showtime.setCreatedAt(now);
        showtime.setUpdatedAt(now);

        Showtime savedShowtime = showtimeRepository.save(showtime);
        showtimeRepository.flush();

        log.info("Showtime created successfully with id: {}", savedShowtime.getId());
        return showtimeMapper.toDTO(savedShowtime);
    }

    @Transactional(readOnly = true)
    public ShowtimeDTO getShowtimeById(UUID id) {
        log.info("Getting showtime by id: {}", id);
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", "id", id));
        return showtimeMapper.toDTO(showtime);
    }

    @Transactional(readOnly = true)
    public List<ShowtimeDTO> getShowtimesByMovieId(UUID movieId) {
        log.info("Getting showtimes by movie id: {}", movieId);
        List<Showtime> showtimes = showtimeRepository.findByMovieId(movieId);
        return showtimes.stream()
                .map(showtimeMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShowtimeDTO> getShowtimesByRoomId(UUID roomId) {
        log.info("Getting showtimes by room id: {}", roomId);
        List<Showtime> showtimes = showtimeRepository.findByRoomId(roomId);
        return showtimes.stream()
                .map(showtimeMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<ShowtimeDTO> searchShowtimes(ShowtimeSearchRequest request, int page, int size, String sortBy, String sortDirection) {
        log.info("Searching showtimes with filters");

        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Specification<Showtime> spec = buildSpecification(request);

        Page<Showtime> showtimePage = showtimeRepository.findAll(spec, pageable);
        List<ShowtimeDTO> content = showtimePage.getContent().stream()
                .map(showtimeMapper::toDTO)
                .collect(Collectors.toList());

        return PageResponse.<ShowtimeDTO>builder()
                .content(content)
                .page(showtimePage.getNumber())
                .size(showtimePage.getSize())
                .totalElements(showtimePage.getTotalElements())
                .totalPages(showtimePage.getTotalPages())
                .build();
    }

    public ShowtimeDTO updateShowtime(UUID id, UpdateShowtimeRequest request) {
        log.info("Updating showtime with id: {}", id);
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", "id", id));

        // Cập nhật startTime nếu có
        if (request.getStartTime() != null) {
            LocalDateTime newStartTime = request.getStartTime();
            LocalDateTime newEndTime = newStartTime.plusMinutes(showtime.getMovie().getDuration());

            // Kiểm tra conflict (trừ chính showtime hiện tại)
            List<Showtime> conflictingShowtimes = showtimeRepository.findByRoomIdAndTimeRange(
                    showtime.getRoom().getId(), newStartTime, newEndTime);

            conflictingShowtimes.removeIf(s -> s.getId().equals(id));
            if (!conflictingShowtimes.isEmpty()) {
                throw new ConflictException("Phòng chiếu đã có lịch chiếu trong khoảng thời gian này");
            }

            showtime.setStartTime(newStartTime);
            showtime.setEndTime(newEndTime);
        }

        // Cập nhật price nếu có
        if (request.getPrice() != null) {
            showtime.setPrice(request.getPrice());
        }

        // Cập nhật status nếu có
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                showtime.setStatus(Showtime.ShowtimeStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Trạng thái không hợp lệ: " + request.getStatus());
            }
        }

        // Cập nhật format nếu có
        if (request.getFormat() != null) {
            showtime.setFormat(request.getFormat());
        }

        showtime.setUpdatedAt(LocalDateTime.now());
        Showtime updatedShowtime = showtimeRepository.save(showtime);
        showtimeRepository.flush();

        log.info("Showtime updated successfully with id: {}", updatedShowtime.getId());
        return showtimeMapper.toDTO(updatedShowtime);
    }

    public void deleteShowtime(UUID id) {
        log.info("Deleting showtime with id: {}", id);
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", "id", id));

        // Kiểm tra xem có booking nào chưa
        if (!showtime.getBookings().isEmpty()) {
            throw new BadRequestException("Không thể xóa lịch chiếu đã có đặt vé");
        }

        showtimeRepository.delete(showtime);
        showtimeRepository.flush();
        log.info("Showtime deleted successfully with id: {}", id);
    }

    @Transactional(readOnly = true)
    public PageResponse<ShowtimeDTO> getAllShowtimes(int page, int size, String sortBy, String sortDirection) {
        log.info("Getting all showtimes with pagination - page: {}, size: {}", page, size);
        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<Showtime> showtimePage = showtimeRepository.findAll(pageable);
        return PageUtil.toPageResponse(showtimePage.map(showtimeMapper::toDTO));
    }

    private Specification<Showtime> buildSpecification(ShowtimeSearchRequest request) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            if (request.getMovieId() != null) {
                predicates.add(cb.equal(root.get("movie").get("id"), request.getMovieId()));
            }

            if (request.getCinemaId() != null) {
                predicates.add(cb.equal(root.get("room").get("cinema").get("id"), request.getCinemaId()));
            }

            if (request.getRoomId() != null) {
                predicates.add(cb.equal(root.get("room").get("id"), request.getRoomId()));
            }

            if (request.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), request.getStartDate()));
            }

            if (request.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startTime"), request.getEndDate()));
            }

            if (request.getStatus() != null && !request.getStatus().isBlank()) {
                try {
                    Showtime.ShowtimeStatus status = Showtime.ShowtimeStatus.valueOf(request.getStatus().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), status));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid status
                }
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
