package com.cinego.server.domain.notification.mapper;

import com.cinego.server.domain.notification.dto.NotificationDTO;
import com.cinego.server.domain.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "userId", source = "user.id")
    NotificationDTO toDTO(Notification notification);
}
