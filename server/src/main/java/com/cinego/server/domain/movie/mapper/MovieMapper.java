package com.cinego.server.domain.movie.mapper;

import com.cinego.server.domain.movie.dto.MovieDTO;
import com.cinego.server.domain.movie.entity.Movie;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MovieMapper {

    MovieDTO toDTO(Movie movie);
}

