package com.cinego.server.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private UUID id;
    private UUID userId;
    private String userFullName;
    private String userAvatarUrl;
    private UUID movieId;
    private String movieTitle;
    private Integer rating;
    private String comment;
    private Boolean isVerified;
    private Integer helpfulCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
