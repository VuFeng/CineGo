package com.cinego.server.domain.movie.dto;

import com.cinego.server.domain.movie.entity.Movie.AgeRating;
import com.cinego.server.domain.movie.entity.Movie.MovieStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    private UUID id;
    private String title;
    private Integer duration;
    private String description;
    private String posterUrl;
    private LocalDate releaseDate;
    private LocalDate endDate;
    private AgeRating rating;
    private String language;
    private String director;
    private String cast;
    private String trailerUrl;
    private MovieStatus status;
    private BigDecimal imdbRating;
    private String country;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

