package com.cinego.server.domain.review.controller;

import com.cinego.server.common.dto.ApiResponse;
import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.domain.review.dto.CreateReviewRequest;
import com.cinego.server.domain.review.dto.ReviewDTO;
import com.cinego.server.domain.review.dto.UpdateReviewRequest;
import com.cinego.server.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Validated
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDTO>> createReview(
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewDTO review = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo đánh giá thành công", review));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReviewDTO>>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        PageResponse<ReviewDTO> result = reviewService.getAllReviews(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<ApiResponse<PageResponse<ReviewDTO>>> getReviewsByMovie(
            @PathVariable @NotNull(message = "Movie ID không được để trống") UUID movieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        PageResponse<ReviewDTO> result = reviewService.getReviewsByMovie(
                movieId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/my-reviews")
    public ResponseEntity<ApiResponse<PageResponse<ReviewDTO>>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        PageResponse<ReviewDTO> result = reviewService.getMyReviews(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/movie/{movieId}/stats")
    public ResponseEntity<ApiResponse<ReviewStatsDTO>> getReviewStats(
            @PathVariable @NotNull(message = "Movie ID không được để trống") UUID movieId) {
        Double avgRating = reviewService.getAverageRating(movieId);
        Long reviewCount = reviewService.getReviewCount(movieId);
        ReviewStatsDTO stats = new ReviewStatsDTO(avgRating, reviewCount);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewDTO>> getReviewById(
            @PathVariable @NotNull(message = "Review ID không được để trống") UUID id) {
        ReviewDTO review = reviewService.getReviewById(id);
        return ResponseEntity.ok(ApiResponse.success(review));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewDTO>> updateReview(
            @PathVariable @NotNull(message = "Review ID không được để trống") UUID id,
            @Valid @RequestBody UpdateReviewRequest request) {
        ReviewDTO review = reviewService.updateReview(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật đánh giá thành công", review));
    }

    @PutMapping("/{id}/helpful")
    public ResponseEntity<ApiResponse<ReviewDTO>> incrementHelpfulCount(
            @PathVariable @NotNull(message = "Review ID không được để trống") UUID id) {
        ReviewDTO review = reviewService.incrementHelpfulCount(id);
        return ResponseEntity.ok(ApiResponse.success("Đánh giá hữu ích", review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable @NotNull(message = "Review ID không được để trống") UUID id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa đánh giá thành công", null));
    }

    // Inner class for stats
    public static class ReviewStatsDTO {
        private Double averageRating;
        private Long reviewCount;

        public ReviewStatsDTO(Double averageRating, Long reviewCount) {
            this.averageRating = averageRating;
            this.reviewCount = reviewCount;
        }

        public Double getAverageRating() {
            return averageRating;
        }

        public void setAverageRating(Double averageRating) {
            this.averageRating = averageRating;
        }

        public Long getReviewCount() {
            return reviewCount;
        }

        public void setReviewCount(Long reviewCount) {
            this.reviewCount = reviewCount;
        }
    }
}
