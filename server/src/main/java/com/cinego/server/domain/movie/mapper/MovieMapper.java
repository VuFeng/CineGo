package com.cinego.server.domain.movie.mapper;

import com.cinego.server.domain.genre.dto.GenreDTO;
import com.cinego.server.domain.genre.mapper.GenreMapper;
import com.cinego.server.domain.movie.dto.MovieDTO;
import com.cinego.server.domain.movie.entity.Movie;
import com.cinego.server.domain.movie.entity.MovieGenre;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {GenreMapper.class})
public interface MovieMapper {

    MovieDTO toDTO(Movie movie);

    @AfterMapping
    default void mapGenres(Movie movie, @MappingTarget MovieDTO movieDTO) {
        if (movie.getMovieGenres() != null && !movie.getMovieGenres().isEmpty()) {
            List<GenreDTO> genres = movie.getMovieGenres().stream()
                    .map(MovieGenre::getGenre)
                    .map(genre -> {
                        GenreDTO dto = new GenreDTO();
                        dto.setId(genre.getId());
                        dto.setName(genre.getName());
                        dto.setSlug(genre.getSlug());
                        dto.setDescription(genre.getDescription());
                        dto.setCreatedAt(genre.getCreatedAt());
                        dto.setUpdatedAt(genre.getUpdatedAt());
                        return dto;
                    })
                    .collect(Collectors.toList());
            movieDTO.setGenres(genres);
        }
    }
}

