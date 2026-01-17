package com.cinego.server.domain.movie.controller;

import com.cinego.server.common.dto.ApiResponse;
import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.domain.movie.dto.CreateMovieRequest;
import com.cinego.server.domain.movie.dto.MovieDTO;
import com.cinego.server.domain.movie.dto.MovieSearchRequest;
import com.cinego.server.domain.movie.dto.UpdateMovieRequest;
import com.cinego.server.domain.movie.service.MovieService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

import com.cinego.server.domain.movie.entity.Movie.MovieStatus;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
@Validated
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<ApiResponse<MovieDTO>> createMovie(
            @Valid @RequestBody CreateMovieRequest request) {
        MovieDTO movie = movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo phim thành công", movie));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MovieDTO>>> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        PageResponse<MovieDTO> result = movieService.getAllMovies(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieDTO>> getMovieById(
            @PathVariable @NotNull(message = "Movie ID không được để trống") UUID id) {
        MovieDTO movie = movieService.getMovieById(id);
        return ResponseEntity.ok(ApiResponse.success(movie));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<MovieDTO>>> searchMovies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MovieStatus status,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        MovieSearchRequest searchRequest = MovieSearchRequest.builder()
                .keyword(keyword)
                .status(status)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();
        PageResponse<MovieDTO> result = movieService.searchMovies(searchRequest, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieDTO>> updateMovie(
            @PathVariable @NotNull(message = "Movie ID không được để trống") UUID id,
            @Valid @RequestBody UpdateMovieRequest request) {
        MovieDTO movie = movieService.updateMovie(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật phim thành công", movie));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(
            @PathVariable @NotNull(message = "Movie ID không được để trống") UUID id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa phim thành công", null));
    }
}

