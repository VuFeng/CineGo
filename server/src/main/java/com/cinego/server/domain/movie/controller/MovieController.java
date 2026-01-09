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

import java.util.UUID;

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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieDTO>> getMovieById(
            @PathVariable @NotNull(message = "Movie ID không được để trống") UUID id) {
        MovieDTO movie = movieService.getMovieById(id);
        return ResponseEntity.ok(ApiResponse.success(movie));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<MovieDTO>>> searchMovies(
            @Valid @RequestBody MovieSearchRequest request) {
        PageResponse<MovieDTO> result = movieService.searchMovies(request);
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

