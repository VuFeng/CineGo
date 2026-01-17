package com.cinego.server.domain.movie.service;

import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.common.exception.ResourceNotFoundException;
import com.cinego.server.common.util.PageUtil;
import com.cinego.server.domain.genre.entity.Genre;
import com.cinego.server.domain.genre.repository.GenreRepository;
import com.cinego.server.domain.genre.dto.GenreDTO;
import com.cinego.server.domain.genre.mapper.GenreMapper;
import com.cinego.server.domain.movie.dto.CreateMovieRequest;
import com.cinego.server.domain.movie.dto.MovieDTO;
import com.cinego.server.domain.movie.dto.MovieSearchRequest;
import com.cinego.server.domain.movie.dto.UpdateMovieRequest;
import com.cinego.server.domain.movie.entity.Movie;
import com.cinego.server.domain.movie.entity.Movie.MovieStatus;
import com.cinego.server.domain.movie.entity.MovieGenre;
import com.cinego.server.domain.movie.mapper.MovieMapper;
import com.cinego.server.domain.movie.repository.MovieGenreRepository;
import com.cinego.server.domain.movie.repository.MovieRepository;
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
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;
    private final GenreRepository genreRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final GenreMapper genreMapper;

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
        movieRepository.flush(); // Flush để có ID

        // Xử lý genres nếu có
        if (request.getGenreIds() != null && !request.getGenreIds().isEmpty()) {
            log.info("Creating genres for movie: {}, genreIds: {}", saved.getId(), request.getGenreIds());
            updateMovieGenres(saved, request.getGenreIds(), now);
        }

        // Reload entity để có genres mới (nếu có)
        Movie movieWithGenres = movieRepository.findById(saved.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", saved.getId()));

        log.info("Movie created successfully with id: {}", movieWithGenres.getId());
        
        MovieDTO movieDTO = movieMapper.toDTO(movieWithGenres);
        
        // Query genres trực tiếp từ repository và map vào DTO
        List<MovieGenre> movieGenres = movieGenreRepository.findByMovieId(saved.getId());
        log.info("Found {} movieGenres from repository for movie: {}", movieGenres.size(), saved.getId());
        
        if (!movieGenres.isEmpty()) {
            List<GenreDTO> genres = movieGenres.stream()
                    .map(MovieGenre::getGenre)
                    .map(genreMapper::toDTO)
                    .collect(Collectors.toList());
            movieDTO.setGenres(genres);
            log.info("Genres mapped successfully: {}", genres.size());
        } else {
            log.warn("No genres found for movie: {}", saved.getId());
        }
        
        return movieDTO;
    }

    /**
     * Cập nhật genres cho movie
     */
    private void updateMovieGenres(Movie movie, List<UUID> genreIds, LocalDateTime now) {
        // Xóa các genres cũ
        movieGenreRepository.deleteByMovieId(movie.getId());
        movieRepository.flush();

        // Nếu genreIds rỗng thì chỉ xóa, không tạo mới
        if (genreIds.isEmpty()) {
            log.info("Removed all genres from movie: {}", movie.getId());
            return;
        }

        // Tạo các MovieGenre mới
        List<MovieGenre> movieGenres = new ArrayList<>();
        for (UUID genreId : genreIds) {
            Genre genre = genreRepository.findById(genreId)
                    .orElseThrow(() -> new ResourceNotFoundException("Genre", "id", genreId));

            MovieGenre movieGenre = new MovieGenre();
            movieGenre.setMovie(movie);
            movieGenre.setGenre(genre);
            movieGenre.setCreatedAt(now);
            movieGenre.setUpdatedAt(now);
            movieGenres.add(movieGenre);
        }

        movieGenreRepository.saveAll(movieGenres);
        movieGenreRepository.flush();
        log.info("Updated {} genres for movie: {}", movieGenres.size(), movie.getId());
    }

    public MovieDTO getMovieById(UUID id) {
        log.info("Getting movie by id: {}", id);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        
        MovieDTO movieDTO = movieMapper.toDTO(movie);
        
        // Map genres manually nếu có
        if (movie.getMovieGenres() != null && !movie.getMovieGenres().isEmpty()) {
            List<GenreDTO> genres = movie.getMovieGenres().stream()
                    .map(MovieGenre::getGenre)
                    .map(genreMapper::toDTO)
                    .collect(Collectors.toList());
            movieDTO.setGenres(genres);
        }
        
        return movieDTO;
    }

    public PageResponse<MovieDTO> searchMovies(MovieSearchRequest request, int page, int size, String sortBy, String sortDirection) {
        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);

        String keyword = request.getKeyword() != null ? request.getKeyword().trim() : null;
        MovieStatus status = request.getStatus();

        Page<Movie> moviePage;
        if (status != null && keyword != null && !keyword.isEmpty()) {
            moviePage = movieRepository.findByStatusAndTitleContainingIgnoreCase(status, keyword, pageable);
        } else if (status != null) {
            moviePage = movieRepository.findByStatus(status, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            moviePage = movieRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        } else {
            moviePage = movieRepository.findAll(pageable);
        }

        return PageUtil.toPageResponse(moviePage.map(movieMapper::toDTO));
    }

    @Transactional(readOnly = true)
    public PageResponse<MovieDTO> getAllMovies(int page, int size, String sortBy, String sortDirection) {
        log.info("Getting all movies with pagination - page: {}, size: {}", page, size);
        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<Movie> moviePage = movieRepository.findAll(pageable);
        
        // Map movies to DTOs with genres
        List<MovieDTO> movieDTOs = moviePage.getContent().stream()
                .map(movie -> {
                    MovieDTO movieDTO = movieMapper.toDTO(movie);
                    // Map genres manually nếu có
                    if (movie.getMovieGenres() != null && !movie.getMovieGenres().isEmpty()) {
                        List<GenreDTO> genres = movie.getMovieGenres().stream()
                                .map(MovieGenre::getGenre)
                                .map(genreMapper::toDTO)
                                .collect(Collectors.toList());
                        movieDTO.setGenres(genres);
                    }
                    return movieDTO;
                })
                .collect(Collectors.toList());
        
        return PageResponse.<MovieDTO>builder()
                .content(movieDTOs)
                .page(moviePage.getNumber())
                .size(moviePage.getSize())
                .totalElements(moviePage.getTotalElements())
                .totalPages(moviePage.getTotalPages())
                .first(moviePage.isFirst())
                .last(moviePage.isLast())
                .build();
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

        LocalDateTime now = LocalDateTime.now();
        movie.setUpdatedAt(now);

        Movie updated = movieRepository.save(movie);

        // Xử lý genres nếu có (null = không thay đổi, [] = xóa hết, [ids] = cập nhật)
        if (request.getGenreIds() != null) {
            updateMovieGenres(updated, request.getGenreIds(), now);
        }
        
        // Reload để có genres mới
        Movie movieWithGenres = movieRepository.findById(updated.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", updated.getId()));
        
        MovieDTO movieDTO = movieMapper.toDTO(movieWithGenres);
        
        // Map genres manually nếu có
        if (movieWithGenres.getMovieGenres() != null && !movieWithGenres.getMovieGenres().isEmpty()) {
            List<GenreDTO> genres = movieWithGenres.getMovieGenres().stream()
                    .map(MovieGenre::getGenre)
                    .map(genreMapper::toDTO)
                    .collect(Collectors.toList());
            movieDTO.setGenres(genres);
        }
        
        return movieDTO;
    }

    @Transactional
    public void deleteMovie(UUID id) {
        log.info("Deleting movie with id: {}", id);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        movieRepository.delete(movie);
    }
}

