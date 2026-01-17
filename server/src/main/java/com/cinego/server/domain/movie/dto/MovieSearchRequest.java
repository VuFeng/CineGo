package com.cinego.server.domain.movie.dto;

import com.cinego.server.domain.movie.entity.Movie.MovieStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieSearchRequest {

    private String keyword;

    private MovieStatus status;

    private LocalDate fromDate;

    private LocalDate toDate;
}

