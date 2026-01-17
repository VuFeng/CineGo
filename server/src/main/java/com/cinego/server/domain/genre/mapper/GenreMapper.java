package com.cinego.server.domain.genre.mapper;

import com.cinego.server.domain.genre.dto.GenreDTO;
import com.cinego.server.domain.genre.entity.Genre;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GenreMapper {
    GenreDTO toDTO(Genre genre);
}
