package com.cinego.server.domain.genre.service;

import com.cinego.server.common.exception.ConflictException;
import com.cinego.server.common.exception.ResourceNotFoundException;
import com.cinego.server.domain.genre.dto.CreateGenreRequest;
import com.cinego.server.domain.genre.dto.GenreDTO;
import com.cinego.server.domain.genre.dto.UpdateGenreRequest;
import com.cinego.server.domain.genre.entity.Genre;
import com.cinego.server.domain.genre.mapper.GenreMapper;
import com.cinego.server.domain.genre.repository.GenreRepository;
import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.common.util.PageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class GenreService {

    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    public GenreDTO createGenre(CreateGenreRequest request) {
        log.info("Creating genre with name: {}", request.getName());

        // Kiểm tra name đã tồn tại
        if (genreRepository.existsByName(request.getName())) {
            throw new ConflictException("Thể loại với tên này đã tồn tại");
        }

        // Kiểm tra slug đã tồn tại
        if (genreRepository.existsBySlug(request.getSlug())) {
            throw new ConflictException("Thể loại với slug này đã tồn tại");
        }

        Genre genre = new Genre();
        genre.setName(request.getName());
        genre.setSlug(request.getSlug());
        genre.setDescription(request.getDescription());

        LocalDateTime now = LocalDateTime.now();
        genre.setCreatedAt(now);
        genre.setUpdatedAt(now);

        Genre savedGenre = genreRepository.save(genre);
        genreRepository.flush();

        log.info("Genre created successfully with id: {}", savedGenre.getId());
        return genreMapper.toDTO(savedGenre);
    }

    @Transactional(readOnly = true)
    public GenreDTO getGenreById(UUID id) {
        log.info("Getting genre by id: {}", id);
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", "id", id));
        return genreMapper.toDTO(genre);
    }

    @Transactional(readOnly = true)
    public PageResponse<GenreDTO> getAllGenres(int page, int size, String sortBy, String sortDirection) {
        log.info("Getting all genres with pagination");
        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<Genre> genrePage = genreRepository.findAll(pageable);
        
        List<GenreDTO> content = genrePage.getContent().stream()
                .map(genreMapper::toDTO)
                .collect(Collectors.toList());
        
        return PageResponse.<GenreDTO>builder()
                .content(content)
                .page(genrePage.getNumber())
                .size(genrePage.getSize())
                .totalElements(genrePage.getTotalElements())
                .totalPages(genrePage.getTotalPages())
                .first(genrePage.isFirst())
                .last(genrePage.isLast())
                .build();
    }

    public GenreDTO updateGenre(UUID id, UpdateGenreRequest request) {
        log.info("Updating genre with id: {}", id);
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", "id", id));

        // Cập nhật name nếu có
        if (request.getName() != null && !request.getName().isBlank()) {
            if (!request.getName().equals(genre.getName()) && genreRepository.existsByName(request.getName())) {
                throw new ConflictException("Thể loại với tên này đã tồn tại");
            }
            genre.setName(request.getName());
        }

        // Cập nhật slug nếu có
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            if (!request.getSlug().equals(genre.getSlug()) && genreRepository.existsBySlug(request.getSlug())) {
                throw new ConflictException("Thể loại với slug này đã tồn tại");
            }
            genre.setSlug(request.getSlug());
        }

        // Cập nhật description nếu có
        if (request.getDescription() != null) {
            genre.setDescription(request.getDescription());
        }

        genre.setUpdatedAt(LocalDateTime.now());
        Genre updatedGenre = genreRepository.save(genre);
        genreRepository.flush();

        log.info("Genre updated successfully with id: {}", updatedGenre.getId());
        return genreMapper.toDTO(updatedGenre);
    }

    public void deleteGenre(UUID id) {
        log.info("Deleting genre with id: {}", id);
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", "id", id));

        // Kiểm tra xem có movie nào đang dùng genre này không
        if (!genre.getMovieGenres().isEmpty()) {
            throw new ConflictException("Không thể xóa thể loại đang được sử dụng bởi các phim");
        }

        genreRepository.delete(genre);
        genreRepository.flush();
        log.info("Genre deleted successfully with id: {}", id);
    }
}
