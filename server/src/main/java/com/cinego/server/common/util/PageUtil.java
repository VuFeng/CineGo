package com.cinego.server.common.util;

import com.cinego.server.common.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public class PageUtil {

    public static Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(sortDirection != null && sortDirection.equalsIgnoreCase("DESC")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC,
                sortBy != null ? sortBy : "createdAt");

        return PageRequest.of(page, size, sort);
    }

    public static <T> PageResponse<T> toPageResponse(Page<T> page) {
        return PageResponse.of(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    public static <T> PageResponse<T> toPageResponse(List<T> content, int page, int size, long totalElements) {
        return PageResponse.of(content, page, size, totalElements);
    }
}
