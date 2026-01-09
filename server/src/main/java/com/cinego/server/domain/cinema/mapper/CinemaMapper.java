package com.cinego.server.domain.cinema.mapper;

import com.cinego.server.domain.cinema.dto.CinemaDTO;
import com.cinego.server.domain.cinema.entity.Cinema;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CinemaMapper {

    CinemaDTO toDTO(Cinema cinema);
}

