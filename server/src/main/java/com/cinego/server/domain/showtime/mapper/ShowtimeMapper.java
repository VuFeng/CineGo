package com.cinego.server.domain.showtime.mapper;

import com.cinego.server.domain.showtime.dto.ShowtimeDTO;
import com.cinego.server.domain.showtime.entity.Showtime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShowtimeMapper {

    @Mapping(target = "movieId", source = "movie.id")
    @Mapping(target = "movieTitle", source = "movie.title")
    @Mapping(target = "movieDuration", source = "movie.duration")
    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "roomName", source = "room.name")
    @Mapping(target = "cinemaId", source = "room.cinema.id")
    @Mapping(target = "cinemaName", source = "room.cinema.name")
    @Mapping(target = "status", source = "status", defaultValue = "ACTIVE")
    ShowtimeDTO toDTO(Showtime showtime);
}
