package com.cinego.server.domain.movie.service;

import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.common.exception.ResourceNotFoundException;
import com.cinego.server.common.util.PageUtil;
import com.cinego.server.domain.movie.dto.CreateMovieRequest;
import com.cinego.server.domain.movie.dto.MovieDTO;
import com.cinego.server.domain.movie.dto.MovieSearchRequest;
import com.cinego.server.domain.movie.dto.UpdateMovieRequest;
import com.cinego.server.domain.movie.entity.Movie;
import com.cinego.server.domain.movie.entity.Movie.MovieStatus;
import com.cinego.server.domain.movie.mapper.MovieMapper;
import com.cinego.server.domain.movie.repository.MovieRepository;
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
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;

    @Transactional
    public MovieDTO createMovie(CreateMovieRequest request) {
        log.info("Creating movie with title: {}", request.getTitle());

        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setDuration(request.getDuration());
        movie.setDescription(request.getDescription());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setEndDate(request.getEndDate());
        movie.setRating(request.getRating());
        movie.setLanguage(request.getLanguage() != null ? request.getLanguage() : "vi");
        movie.setDirector(request.getDirector());
        movie.setCast(request.getCast());
        movie.setTrailerUrl(request.getTrailerUrl());
        movie.setStatus(request.getStatus() != null ? request.getStatus() : MovieStatus.COMING_SOON);
        movie.setImdbRating(request.getImdbRating());
        movie.setCountry(request.getCountry());

        // Set createdAt/updatedAt thủ công cho Supabase pooler
        LocalDateTime now = LocalDateTime.now();
        movie.setCreatedAt(now);
        movie.setUpdatedAt(now);

        Movie saved = movieRepository.save(movie);
        log.info("Movie created successfully with id: {}", saved.getId());
        return movieMapper.toDTO(saved);
    }

    public MovieDTO getMovieById(UUID id) {
        log.info("Getting movie by id: {}", id);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        return movieMapper.toDTO(movie);
    }

    public PageResponse<MovieDTO> searchMovies(MovieSearchRequest request) {
        Pageable pageable = PageUtil.createPageable(
                request.getPage().getPage(),
                request.getPage().getSize(),
                request.getPage().getSortBy(),
                request.getPage().getSortDirection()
        );

        String keyword = request.getKeyword() != null ? request.getKeyword().trim() : null;
        MovieStatus status = request.getStatus();

        Page<Movie> page;
        if (status != null && keyword != null && !keyword.isEmpty()) {
            page = movieRepository.findByStatusAndTitleContainingIgnoreCase(status, keyword, pageable);
        } else if (status != null) {
            page = movieRepository.findByStatus(status, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            page = movieRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        } else {
            page = movieRepository.findAll(pageable);
        }

        return PageUtil.toPageResponse(page.map(movieMapper::toDTO));
    }

    @Transactional
    public MovieDTO updateMovie(UUID id, UpdateMovieRequest request) {
        log.info("Updating movie with id: {}", id);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            movie.setTitle(request.getTitle());
        }
        if (request.getDuration() != null) {
            movie.setDuration(request.getDuration());
        }
        if (request.getDescription() != null) {
            movie.setDescription(request.getDescription());
        }
        if (request.getPosterUrl() != null) {
            movie.setPosterUrl(request.getPosterUrl());
        }
        if (request.getReleaseDate() != null) {
            movie.setReleaseDate(request.getReleaseDate());
        }
        if (request.getEndDate() != null) {
            movie.setEndDate(request.getEndDate());
        }
        if (request.getRating() != null) {
            movie.setRating(request.getRating());
        }
        if (request.getLanguage() != null) {
            movie.setLanguage(request.getLanguage());
        }
        if (request.getDirector() != null) {
            movie.setDirector(request.getDirector());
        }
        if (request.getCast() != null) {
            movie.setCast(request.getCast());
        }
        if (request.getTrailerUrl() != null) {
            movie.setTrailerUrl(request.getTrailerUrl());
        }
        if (request.getStatus() != null) {
            movie.setStatus(request.getStatus());
        }
        if (request.getImdbRating() != null) {
            movie.setImdbRating(request.getImdbRating());
        }
        if (request.getCountry() != null) {
            movie.setCountry(request.getCountry());
        }

        movie.setUpdatedAt(LocalDateTime.now());

        Movie updated = movieRepository.save(movie);
        return movieMapper.toDTO(updated);
    }

    @Transactional
    public void deleteMovie(UUID id) {
        log.info("Deleting movie with id: {}", id);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        movieRepository.delete(movie);
    }
}

