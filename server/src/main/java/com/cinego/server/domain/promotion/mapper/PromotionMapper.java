package com.cinego.server.domain.promotion.mapper;

import com.cinego.server.domain.promotion.dto.PromotionDTO;
import com.cinego.server.domain.promotion.entity.Promotion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    @Mapping(target = "discountType", source = "discountType", defaultValue = "PERCENTAGE")
    @Mapping(target = "applicableMovies", expression = "java(toUUIDList(promotion.getApplicableMovies()))")
    PromotionDTO toDTO(Promotion promotion);

    default List<UUID> toUUIDList(UUID[] array) {
        if (array == null) {
            return null;
        }
        return Arrays.asList(array);
    }
}
