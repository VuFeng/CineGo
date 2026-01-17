package com.cinego.server.domain.review.mapper;

import com.cinego.server.domain.review.dto.ReviewDTO;
import com.cinego.server.domain.review.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "userAvatarUrl", source = "user.avatarUrl")
    @Mapping(target = "movieId", source = "movie.id")
    @Mapping(target = "movieTitle", source = "movie.title")
    ReviewDTO toDTO(Review review);
}
