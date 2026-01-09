package com.cinego.server.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest {
    @Min(value = 0, message = "Số trang phải >= 0")
    @Builder.Default
    private int page = 0;

    @Min(value = 1, message = "Kích thước trang phải >= 1")
    @Max(value = 100, message = "Kích thước trang phải <= 100")
    @Builder.Default
    private int size = 20;

    private String sortBy;
    private String sortDirection; // ASC, DESC
}
