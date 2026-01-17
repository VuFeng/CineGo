package com.cinego.server.domain.genre.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGenreRequest {
    private String name;
    private String slug;
    private String description;
}
