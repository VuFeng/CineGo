package com.cinego.server.domain.user.mapper;

import com.cinego.server.domain.user.dto.UserDTO;
import com.cinego.server.domain.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDTO(User user);
}
