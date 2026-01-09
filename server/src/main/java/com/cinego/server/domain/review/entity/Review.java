package com.cinego.server.domain.review.entity;

import com.cinego.server.common.entity.BaseEntity;
import com.cinego.server.domain.movie.entity.Movie;
import com.cinego.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "reviews",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_movie_review",
                        columnNames = {"user_id", "movie_id"}
                )
        },
        indexes = {
                @Index(name = "idx_review_movie", columnList = "movie_id, created_at"),
                @Index(name = "idx_review_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1-5 stars

    @Column(name = "comment", columnDefinition = "text")
    private String comment;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false; // Đã xem phim chưa

    @Column(name = "helpful_count", nullable = false)
    private Integer helpfulCount = 0;
}
