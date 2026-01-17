package com.cinego.server.domain.promotion.controller;

import com.cinego.server.common.dto.ApiResponse;
import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.domain.promotion.dto.CreatePromotionRequest;
import com.cinego.server.domain.promotion.dto.PromotionDTO;
import com.cinego.server.domain.promotion.dto.PromotionSearchRequest;
import com.cinego.server.domain.promotion.dto.UpdatePromotionRequest;
import com.cinego.server.domain.promotion.service.PromotionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
@Validated
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    public ResponseEntity<ApiResponse<PromotionDTO>> createPromotion(
            @Valid @RequestBody CreatePromotionRequest request) {
        PromotionDTO promotion = promotionService.createPromotion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo khuyến mãi thành công", promotion));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<PromotionDTO>>> getAllActivePromotions() {
        List<PromotionDTO> promotions = promotionService.getAllActivePromotions();
        return ResponseEntity.ok(ApiResponse.success(promotions));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PromotionDTO>>> getAllPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        PageResponse<PromotionDTO> result = promotionService.getAllPromotions(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PromotionDTO>> getPromotionById(
            @PathVariable @NotNull(message = "Promotion ID không được để trống") UUID id) {
        PromotionDTO promotion = promotionService.getPromotionById(id);
        return ResponseEntity.ok(ApiResponse.success(promotion));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<PromotionDTO>> getPromotionByCode(
            @PathVariable @NotBlank(message = "Promotion code không được để trống") String code) {
        PromotionDTO promotion = promotionService.getPromotionByCode(code);
        return ResponseEntity.ok(ApiResponse.success(promotion));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<PromotionDTO>>> searchPromotions(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) UUID movieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        PromotionSearchRequest searchRequest = PromotionSearchRequest.builder()
                .code(code)
                .isActive(isActive)
                .movieId(movieId)
                .build();
        PageResponse<PromotionDTO> result = promotionService.searchPromotions(searchRequest, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PromotionDTO>> updatePromotion(
            @PathVariable @NotNull(message = "Promotion ID không được để trống") UUID id,
            @Valid @RequestBody UpdatePromotionRequest request) {
        PromotionDTO promotion = promotionService.updatePromotion(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật khuyến mãi thành công", promotion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(
            @PathVariable @NotNull(message = "Promotion ID không được để trống") UUID id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa khuyến mãi thành công", null));
    }
}
