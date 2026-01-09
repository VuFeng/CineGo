package com.cinego.server.domain.movie.dto;

import com.cinego.server.domain.movie.entity.Movie.AgeRating;
import com.cinego.server.domain.movie.entity.Movie.MovieStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMovieRequest {

    @Size(max = 255, message = "Tiêu đề phim không được vượt quá 255 ký tự")
    private String title;

    @Min(value = 1, message = "Thời lượng phim phải lớn hơn 0 phút")
    @Max(value = 600, message = "Thời lượng phim không được vượt quá 600 phút")
    private Integer duration;

    @Size(max = 2000, message = "Mô tả phim không được vượt quá 2000 ký tự")
    private String description;

    private String posterUrl;

    private LocalDate releaseDate;

    private LocalDate endDate;

    private AgeRating rating;

    @Size(max = 16, message = "Mã ngôn ngữ không được vượt quá 16 ký tự")
    private String language;

    private String director;

    private String cast;

    private String trailerUrl;

    private MovieStatus status;

    private BigDecimal imdbRating;

    private String country;
}

