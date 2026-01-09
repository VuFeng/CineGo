package com.cinego.server.domain.room.mapper;

import com.cinego.server.domain.room.dto.RoomDTO;
import com.cinego.server.domain.room.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(target = "cinemaId", source = "cinema.id")
    RoomDTO toDTO(Room room);
}

