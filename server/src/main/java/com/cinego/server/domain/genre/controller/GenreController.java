package com.cinego.server.domain.genre.controller;

import com.cinego.server.common.dto.ApiResponse;
import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.domain.genre.dto.CreateGenreRequest;
import com.cinego.server.domain.genre.dto.GenreDTO;
import com.cinego.server.domain.genre.dto.UpdateGenreRequest;
import com.cinego.server.domain.genre.service.GenreService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
@Validated
public class GenreController {

    private final GenreService genreService;

    @PostMapping
    public ResponseEntity<ApiResponse<GenreDTO>> createGenre(
            @Valid @RequestBody CreateGenreRequest request) {
        GenreDTO genre = genreService.createGenre(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo thể loại thành công", genre));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GenreDTO>>> getAllGenres(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        PageResponse<GenreDTO> result = genreService.getAllGenres(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GenreDTO>> getGenreById(
            @PathVariable @NotNull(message = "Genre ID không được để trống") UUID id) {
        GenreDTO genre = genreService.getGenreById(id);
        return ResponseEntity.ok(ApiResponse.success(genre));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GenreDTO>> updateGenre(
            @PathVariable @NotNull(message = "Genre ID không được để trống") UUID id,
            @Valid @RequestBody UpdateGenreRequest request) {
        GenreDTO genre = genreService.updateGenre(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thể loại thành công", genre));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGenre(
            @PathVariable @NotNull(message = "Genre ID không được để trống") UUID id) {
        genreService.deleteGenre(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa thể loại thành công", null));
    }
}
